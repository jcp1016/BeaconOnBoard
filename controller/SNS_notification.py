import json,time,sys
from collections import OrderedDict
from threading import Thread
import pyupm_i2clcd as lcd
import time
import mraa
import boto3
from boto3.dynamodb.conditions import Key,Attr

sys.path.append('./utils')
import tripupdate,vehicle,alert,mtaUpdates,aws
import traceback


### YOUR CODE HERE ####
import sys
import time
import threading
import traceback
from threading import Thread
from Queue import *
import math

Topic = "arn:aws:sns:us-east-1:480370410475:BOB"

danger = Queue()

stop = Queue()

deactivate = Queue()

interval = 180 # 3min
def main():

	myResource = aws.getResource('sns','us-east-1')
	client = aws.getClient('sns','us-east-1')
	response = client.subscribe(
		TopicArn=Topic,
		Protocol='SMS',
		Endpoint='1-347-399-4561'
	)

	time.sleep(5)

	tempSensor = mraa.Aio(2)
	switch_pin_number=8
	switch = mraa.Gpio(switch_pin_number)
	switch.dir(mraa.DIR_IN)
	time_prev=0
	threads=[]
	try:
		t1= threading.Thread(target=check_temperature, args=(tempSensor,))
		t1.daemon=True
		threads.append(t1)
		t2= threading.Thread(target=check_deactivate, args=(switch,))
		t2.daemon=True
		threads.append(t2)
		for ele in threads:
			ele.start()
		while stop.empty():
			if not danger.empty() and time.time() - time_prev > interval:
				pub = client.publish( TopicArn = Topic, Message = "Baby in the Car!!! Temperature: "+ str(get_temp(tempSensor))+"!!!" )
				time_prev = time.time()
			if not deactivate.empty():
				danger.get()
				deactivate.get()
	except KeyboardInterrupt:
		stop.put(1)
	for ele in threads:
		ele.join(2)
	print "Ctrl+C happen"
	exit()
	return

def check_deactivate(switch):
	while stop.empty():
		if (switch.read()):
			if deactivate.empty():
				print "deactivate"
				deactivate.put(1)
	return

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
	return temperature


if __name__ == "__main__":
    main()