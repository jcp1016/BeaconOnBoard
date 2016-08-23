#!/bin/sh
sleep 10
killall pppd
sleep 5
python /home/root/BeaconOnBoard/controller/attach_gprs.py
sleep 10
nohup pppd call provider &
sleep 10
ifconfig ppp0 up
sleep 1
python /home/root/BeaconOnBoard/controller/startup_mailer.py
exit 0
