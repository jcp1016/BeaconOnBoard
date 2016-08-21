#!/bin/sh
sleep 10
pppd call provider &
sleep 5
python /home/root/Lab1/startup_mailer.py
exit 0
