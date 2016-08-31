import json,time,sys
from collections import OrderedDict
from threading import Thread
import pyupm_i2clcd as lcd
import time
import mraa
import boto3
from boto3.dynamodb.conditions import Key,Attr

sys.path.append('./utils')
import aws
import traceback


### YOUR CODE HERE ####
import sys
import time
import threading
import traceback
from threading import Thread
from Queue import *
import math
import cv2
import pyupm_biss0001 as upmMotion

DevID = "BOB001"
Topic = "arn:aws:sns:us-east-1:480370410475:"+DevID
S3_client = aws.getClient('s3','us-east-1')
cam = cv2.VideoCapture(0)
s, img = cam.read()
cv2.imwrite('test.png',img)
response = S3_client.upload_file('test.png', 'bobotry', 'BOB001/inside.png')
message = "file uploaded again"
client = aws.getClient('sns','us-east-1')
"""
message_format = r'''{
        "default": "Content",
        "email": "Content",
        "sqs": "Content",
        "lambda": "Content",
        "http": "Content",
        "https": "Content",
        "sms": "Content",
        "APNS": "{\"aps\":{\"alert\": \"Content\"} }",
        "APNS_SANDBOX":"{\"aps\":{\"alert\":\"Content\"}}",
        "APNS_VOIP":"{\"aps\":{\"alert\":\"Content\"}}",
        "APNS_VOIP_SANDBOX": "{\"aps\":{\"alert\": \"Content\"} }",
        "MACOS":"{\"aps\":{\"alert\":\"Content\"}}",
        "MACOS_SANDBOX": "{\"aps\":{\"alert\": \"Content\"} }",
        "GCM": "{ \"data\": { \"message\": \"Content\" } }",
        "ADM": "{ \"data\": { \"message\": \"Content\" } }",
        "BAIDU": "{\"title\":\"Content\",\"description\":\"Content\"}",
        "MPNS" : "<?xml version=\"1.0\" encoding=\"utf-8\"?><wp:Notification xmlns:wp=\"WPNotification\"><wp:Tile><wp:Count>ENTER COUNT</wp:Count><wp:Title>Content</wp:Title></wp:Tile></wp:Notification>",
        "WNS" : "<badge version\"1\" value\"23\"/>"
        }'''
"""
message_format = r'''{
        "default": "Content",
        "sms": "Content",
        "GCM": "{ \"data\": { \"message\": \"Content\" } }"
        }'''
send_message = message_format.replace('Content', message)
response = client.publish(
    TopicArn=Topic,
    Message=send_message,
    MessageStructure='json'
)
myMotion = upmMotion.BISS0001(8)

try:
    while True:
        if (myMotion.value()):
            print "detected"
        else:
            print "nothing"
        time.sleep(1)
except KeyboardInterrupt:
    exit()
