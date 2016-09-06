#
# SendSMS.py
#
# Desc: AWS Lambda function that publishes an SMS text to a topic.
#       Triggered when mycar.jpg is uploaded to S3
#
# Author: Janet Prumachuk
# Date:   August 2016
#
from __future__ import print_function
import boto3

sns_client = boto3.client('sns')

msg_text = 'Tap https://s3.amazonaws.com/bob.jcp.001/index.html to see inside your car.'

response = sns_client.publish(
    TopicArn = 'arn:aws:sns:us-east-1:146208918492:BobJCPTopic',
                Message  = msg_text,
                Subject  = 'Motion Detected in Hot Car!',
                MessageStructure = 'string')
