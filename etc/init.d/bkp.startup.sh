#!/bin/sh
sleep 10
killall pppd
sleep 5
python /home/root/BeaconOnBoard/controller/attach_gprs.py
sleep 20
nohup pppd call provider-simple &
sleep 10
python /home/root/BeaconOnBoard/controller/startup_mailer.py
exit 0
