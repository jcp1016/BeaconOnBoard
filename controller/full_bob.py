# =========================
# Creator: Sheng Qian
# =========================
import cv2
from datetime import datetime
import numpy

import json,time,sys
from collections import OrderedDict
from threading import Thread
import pyupm_i2clcd as lcd
import mraa
import boto3
from boto3.dynamodb.conditions import Key,Attr

sys.path.append('./utils')
import aws
import traceback


### YOUR CODE HERE ####
import threading
import traceback
from Queue import *
import math
import pyupm_biss0001 as upmMotion

# some global values
# Topic_prev = "arn:aws:sns:us-east-1:480370410475:"
Topic = None
Dev_ID = "BOB001"
tableName = "Car_situation"
S3_key = Dev_ID+'/inside.png'
S3_bucket = "bobotry"
danger_start = time.time()
danger = Queue()

stop = Queue()

deactivate = Queue()

camera_on = Queue()

PIR_motion_detection = Queue()
camera_motion_detection = Queue()

PIR_on = Queue()

interval = 20 # 3min

message_format = r'''{
        "default": "Content",
        "sms": "Content",
        "GCM": "{ \"data\": { \"message\": \"Content\" } }"
        }'''

def check_temperature(tempSensor):
	i =0
	danger_threshold = 10
	safe_threshold = 10
	danger_start = time.time()
	safe_start = time.time()
	temp_safe = 0
	temp_danger = 0
	while stop.empty():
		R=0
		R = tempSensor.read()
		time.sleep(0.01)
		R = 1023.0/R-1
		R=100000.0*R
		temperature = 1.0/(math.log(R/100000.0)/4275+1/298.15)-273.15
		if i%20 == 0:
			print temperature

		# if danger status happens, then only when the situation is safe for a time period, then the system thinks the danger status is solved

		if temperature>28:
			if temp_danger ==0:
				danger_start = time.time()
			temp_danger = 1
			temp_safe = 0
			# if danger.empty():
			# 	danger.put(1)
		else:
			if temp_safe == 0:
				safe_start = time.time()
			temp_safe = 1

		if temp_danger:
			if time.time()-danger_start>danger_threshold:
				if danger.empty():
					danger.put(1)
					print "danger put: "+str(temperature)
		if temp_safe:
			if time.time()-safe_start>safe_threshold:
				temp_danger = 0
				danger_start = time.time()
				print "safe"
		time.sleep(0.5)


		i= i+1
	return

def get_temp(tempSensor):
	R=0
	R = tempSensor.read()
	time.sleep(0.01)
	print R
	R = 1023.0/R-1
	print R
	R=100000.0*R
	temperature = 1.0/(math.log(R/100000.0)/4275+1/298.15)-273.15
	return round(temperature,1)

def PIR_motion(motion_sensor):
	detected = 0
	total = 0
	while stop.empty():
		if motion_sensor.value():
			detected = detected+1
			print "detected: "+ str(detected)
		else:
			print "nothing"
		total = total+1
		time.sleep(0.1)
		if (detected>=10):
			if PIR_motion_detection.empty():
				PIR_motion_detection.put(1)
				return
		elif total>=100:
			return
	return

def diffImg(t0, t1, t2):
  d1 = cv2.absdiff(t2, t1)
  # d2 = cv2.absdiff(t1, t0)
  # d3 = cv2.bitwise_and(d1, d2)
  # res = cv2.blur(d3, (5,5))
  res = cv2.blur(d1, (5,5))
  res = cv2.morphologyEx(res, cv2.MORPH_OPEN, None)
  res = cv2.morphologyEx(res, cv2.MORPH_CLOSE, None)
  res = cv2.threshold(res, 10, 255, cv2.THRESH_BINARY_INV)
  #return cv2.addWeighted(d1,0.5,d2,0.5,0)
  return res[1]

def somethingHasMoved(frame, threshold):
    nb=0 #Will hold the number of black pixels
    
    # for x in range(frame.shape[0]): #Iterate the hole image
    #     for y in range(frame.shape[1]):
    #         if frame[x,y] == 0.0: #If the pixel is black keep it
    #             nb += 1
    
    nb=len(numpy.where(frame == 0)[0])
    avg = (nb*100.0)/(frame.shape[0]*frame.shape[1]) #Calculate the average of black pixel in the image

    if avg > threshold:#If over the ceiling trigger the alarm
        return True
    else:
        return False

def camera_motion(S3_client_2):
	cam = cv2.VideoCapture(0)
	s, img = cam.read()
	num_moved = 0
	started = time.time()
	# Read three images first:
	t_minus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
	t = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
	t_plus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
	total = 0
	print "=============camera started=============="
	while stop.empty():
		instant = time.time()
		time.sleep(0.01) 
		frame = diffImg(t_minus, t, t_plus)
		#cv2.imshow(winName, frame)
		# Read next image
		t_minus = t
		t = t_plus
		t_plus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)

		if instant > started +5:
			if somethingHasMoved(frame, 5):#Wait 5 second after the webcam start for luminosity adjusting etc..
				num_moved=num_moved+1
				print datetime.now().strftime("%b %d, %H:%M:%S"), "Something is moving !"
	    # else:
	    #  	num_moved = 0
			total = total+1
			if num_moved>=20:
				s_up, img_up = cam.read()
				cv2.imwrite('test.png',img_up)
				S3_client_2.upload_file('test.png', S3_bucket, S3_key)
				print "file uploaded"
				if camera_motion_detection.empty():
					camera_motion_detection.put(1)
				return
			elif total>=200:
				return
	return

def main():
	warn_send = 0
	myResource = aws.getResource('dynamodb','us-east-1')
	myTable = myResource.Table(tableName)
	client = aws.getClient('sns','us-east-1')
	S3_client = aws.getClient('s3','us-east-1')
	ddb_client = aws.getClient('dynamodb', 'us-east-1')

    # create SNS topic for this device
	CTopic_response = client.create_topic(Name = Dev_ID)
	time.sleep(2)
	if(CTopic_response["ResponseMetadata"]['HTTPStatusCode']==200):
		Topic = CTopic_response["TopicArn"]
	print Topic
	
	# create S3 directory for this device in the bucket "bobotry"
	# copy testimage.jpg as the default image if there is no inside car image uploaded
	results = S3_client.list_objects(Bucket='bobotry', Prefix=S3_key)
	if 'Contents' in results:
		print 'exist'
	else:
		print 'no'
		S3_response = S3_client.copy_object(
			Bucket='bobotry',
			CopySource='bobotry/first/inside.png',
			Key=S3_key
		)
		time.sleep(2)
		print S3_response
	
	tempSensor = mraa.Aio(2)
	#switch_pin_number=8
	#switch = mraa.Gpio(switch_pin_number)
	#switch.dir(mraa.DIR_IN)
	myMotion = upmMotion.BISS0001(8)
	

	time_prev=0
	threads=[]
	try:
		t1= threading.Thread(target=check_temperature, args=(tempSensor,))
		t1.daemon=True
		threads.append(t1)
		#t2= threading.Thread(target=check_deactivate, args=(switch,))
		#t2.daemon=True
		#threads.append(t2)
		for ele in threads:
			ele.start()
		while stop.empty():
			if not danger.empty() and time.time() - time_prev > interval:
				if PIR_on.empty():
					t2 = threading.Thread(target = PIR_motion, args=(myMotion,))
					t2.daemon=True
					threads.append(t2)
					threads[1].start()
					PIR_on.put(1)

				if (not PIR_motion_detection.empty()) and camera_on.empty():
					threads[1].join(1)
					t3 = threading.Thread(target = camera_motion, args=(S3_client,))
					t3.daemon=True
					threads[1]=(t3)
					threads[-1].start()
					camera_on.put(1)
				if (not PIR_motion_detection.empty()) and (not camera_motion_detection.empty()):
					if ( not warn_send):
						threads[1].join(1)
						del threads[1]
						if(Topic!=None):
							message = "Baby in the Car!!! Temperature: "+ str(get_temp(tempSensor))+"\u2103"+"!!!"
							try:
								print "=================="
								
								print get_temp(tempSensor)
								danger_start_time = int(danger_start*1000)
								print danger_start_time
								response = myTable.put_item(
									Item={
									'deviceID': Dev_ID,
									'start_time': danger_start_time,
									'temperature': int(get_temp(tempSensor)*10)
									}
								)
							except Exception as e_db:
								print str(e_db)
								stop.put(1)
							send_message = message_format.replace('Content', message)
							pub = client.publish( TopicArn = Topic, Message =  send_message, MessageStructure='json')

						warn_send = 1
				#time_prev = time.time()

			# how about the system status after the publishing???
			# if not deactivate.empty():
			# 	danger.get()
			# 	deactivate.get()
	except KeyboardInterrupt:
		stop.put(1)
	for ele in threads:
		ele.join(2)
	print "Ctrl+C happen"
	exit()
	return


if __name__ == "__main__":
    main()
