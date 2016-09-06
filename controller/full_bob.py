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
device_act_table_name = "device_action"
car_real_table_name = "car_situation_realtime"
upload_table_name = "Dev_realtime"
S3_key = Dev_ID+'/inside.png'
#S3_bucket = "bobotry"
S3_bucket = "bobotry2"
S3w_bucket = "bob.jcp.001"

danger_start_queue = Queue()
danger = Queue()

stop = Queue()

deactivate = Queue()

camera_on = Queue()

PIR_motion_detection = Queue()
camera_motion_detection = Queue()

active_queue = Queue()

PIR_on = Queue()

cam = cv2.VideoCapture(0)

interval = 20 # 3min

message_format = r'''{
        "default": "Content",
        "sms": "Content",
        "GCM": "{ \"data\": { \"message\": \"Content\" } }"
        }'''

def check_upload(check_table, S3_client):
	upload_checked = 0
	while stop.empty():
		upload_response = check_table.get_item(
			Key = {
			"DevNum": Dev_ID
		})
		if("Item" in upload_response):
			if upload_response["Item"]["Upload"]==1:
				upload_checked = 1
				ret, frame = cam.read()
				cv2.imwrite('realtime.png',frame)
				S3_client.upload_file('realtime.png', S3_bucket, Dev_ID+"/realtime_inside.png")
				response = check_table.put_item(
					Item={
					'DevNum': Dev_ID,
					'Upload': 0,
					'Finished': 1
				})
				time.sleep(1)
		time.sleep(1)
	return

def check_active(active_table):
	while stop.empty():
		dev_act_response = active_table.get_item(
		Key = {
			"DevNum": Dev_ID
		})
		if("Item" in dev_act_response):
			if dev_act_response["Item"]["active"]==1:
				if active_queue.empty():
					active_queue.put(1)
			else:
				if not active_queue.empty():
					active_queue.queue.clear()
		time.sleep(1)
	return

def check_temperature(tempSensor, car_real_table):
	i =0
	danger_threshold = 10
	safe_threshold = 10
	danger_start = time.time()
	safe_start = time.time()
	temp_safe = 0
	temp_danger = 0
	real_time_sent = 0
	while stop.empty():
		if(active_queue.empty()):
			print "=======reset========="
			danger_start = time.time()
			safe_start = time.time()
			temp_safe = 0
			temp_danger = 0
			time.sleep(1)
		R=0
		R = tempSensor.read()
		time.sleep(0.01)
		R = 1023.0/R-1
		R=100000.0*R
		temperature = 1.0/(math.log(R/100000.0)/4275+1/298.15)-273.15
		if i%20 == 0:
			print temperature
		if(time.time()>=real_time_sent+20):
			real_time_sent=time.time()
			response = car_real_table.put_item(
				Item={
				'DevNum': Dev_ID,
				'start_time': int(real_time_sent*1000),
				'temperature': int(get_temp(tempSensor)*10)
				}
			)
		# if danger status happens, then only when the situation is safe for a time period, 
                # then the system thinks the danger status is solved

		if temperature>24:
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
					danger_start_queue.queue.clear()
					danger_start_queue.put(danger_start)
					danger.put(1)
					print "Danger, temperature is: "+str(temperature)
		if temp_safe:
			if time.time()-safe_start>safe_threshold:
				temp_danger = 0
				danger_start = time.time()
				print "Temperature is safe"
		time.sleep(0.5)


		i= i+1
	return

def get_temp(tempSensor):
	R=0
	R = tempSensor.read()
	time.sleep(0.01)
	#print R
	R = 1023.0/R-1
	#print R
	R=100000.0*R
	temperature = 1.0/(math.log(R/100000.0)/4275+1/298.15)-273.15
	return round(temperature,1)

def PIR_motion(motion_sensor):
	detected = 0
	total = 0
	while stop.empty() and not active_queue.empty():
		if motion_sensor.value():
		#if motion_sensor.read():
			detected = detected+1
			print "PIR detected: "+ str(detected)
		else:
			print "PIR detected nothing"
		total = total+1
		time.sleep(0.1)
		if (detected>=3):
			if PIR_motion_detection.empty():
				PIR_motion_detection.put(1)
				return
		#elif total>=100:
		#	return
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
	#cam = cv2.VideoCapture(0)
	s, img = cam.read()
	num_moved = 0
	started = time.time()
	# Read three images first:
	t_minus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
	t = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
	t_plus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
	total = 0
	print "=============camera started=============="
	while stop.empty() and not active_queue.empty():
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
			if num_moved>=1:
				s_up, img_up = cam.read()
				cv2.imwrite('test.png',img_up)
				S3_client_2.upload_file('test.png', S3_bucket, S3_key)
				print "file uploaded"
				if camera_motion_detection.empty():
					camera_motion_detection.put(1)
				return
			#elif total>=500:
			#	return
	return

def main():
	warn_send = 0
	myResource = aws.getResource('dynamodb','us-east-1')

	myTable = myResource.Table(tableName)
	device_action_table = myResource.Table(device_act_table_name)
	car_real_table = myResource.Table(car_real_table_name)
	upload_real_table = myResource.Table(upload_table_name)
	# dev_act_response = device_action_table.get_item(
	# 	Key = {
	# 		"DevNum": Dev_ID
	# 	})

	# automatically set the device active when start up
	dev_act_response = device_action_table.put_item(
		Item={
				'DevNum': Dev_ID,
				'active': 1
			}
	)
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
	results = S3_client.list_objects(Bucket='bobotry2', Prefix=S3_key)
	if 'Contents' in results:
		print 'Bucket exists'
	else:
		print 'No bucket'
		S3_response = S3_client.copy_object(
			Bucket='bobotry2',
			CopySource='bobotry2/first/inside.png',
			Key=S3_key
		)
		time.sleep(2)
		print S3_response
	
	tempSensor = mraa.Aio(2)
	# switch_pin_number=8
	# switch = mraa.Gpio(switch_pin_number)
	# switch.dir(mraa.DIR_IN)
	myMotion = upmMotion.BISS0001(8)
	

	time_prev=0
	threads=[]
	try:
		check_active_thread = threading.Thread(target=check_active, args=(device_action_table,))
		check_active.daemon=True
		check_active_thread.start()
		upload_thread = threading.Thread(target=check_upload, args=(upload_real_table,S3_client,))
		upload_thread.daemon=True
		upload_thread.start()
		t1= threading.Thread(target=check_temperature, args=(tempSensor,car_real_table,))
		t1.daemon=True
		threads.append(t1)
		#t2= threading.Thread(target=check_deactivate, args=(switch,))
		#t2.daemon=True
		#threads.append(t2)
		for ele in threads:
			ele.start()
		print "=======started========"
		while stop.empty():
			if not active_queue.empty():
				if not danger.empty() and time.time() - time_prev > interval:
					if PIR_on.empty():
                                                print "Heat detected.  Starting motion detection."
						#t2 = threading.Thread(target = PIR_motion, args=(switch,))
						t2 = threading.Thread(target = PIR_motion, args=(myMotion,))
						t2.daemon=True
						threads.append(t2)
						threads[1].start()
						PIR_on.put(1)

					if (not PIR_motion_detection.empty()) and camera_on.empty():
                                                print "PIR sensor detected motion. Starting camera motion detection."
						threads[1].join(1)
						t3 = threading.Thread(target = camera_motion, args=(S3_client,))
						t3.daemon=True
						threads[1]=(t3)
						threads[-1].start()
						camera_on.put(1)
					if (not PIR_motion_detection.empty()) and (not camera_motion_detection.empty()):
						if ( not warn_send):
                                                        print "Motion detected.  Uploading image."
							#print "gonna send the temperature: "+str(warn_send)
                                                        S3_client.upload_file('test.png', S3w_bucket, 'www/mycar.png',
                                                                {'ACL':'public-read','ContentType':'image/png'})

							threads[1].join(1)
							del threads[1]
							if(Topic!=None):
								message = "Baby in the Car!!! Temperature: "+ str(get_temp(tempSensor))+"\u2103"+"!!!"
								try:
									print "=================="
									
									print get_temp(tempSensor)
									if(not danger_start_queue.empty()):
										danger_start = danger_start_queue.get()
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
			else:
				print "=======stopped========"
				for ele in threads[1:]:
					ele.join(2)
				threads = [threads[0]]
				danger.queue.clear()
				camera_on.queue.clear()
				PIR_on.queue.clear()
				PIR_motion_detection.queue.clear()
				camera_motion_detection.queue.clear()
				warn_send=0
			time.sleep(0.5)
	except KeyboardInterrupt:
		stop.put(1)
	check_active_thread.join(2)
	for ele in threads:
		ele.join(2)
	print "Ctrl+C happened"
	exit()
	return


if __name__ == "__main__":
    main()
