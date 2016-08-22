1) In order to use GPRS we had to rebuild the Yocto Linux kernel with
ppp enabled.  Rebuilding the kernel takes several days. We will upload the new
image to the course website so others can use it.

2) The GPRS shield has an active SIM card that currently uses a T-Mobile
plan.  The PDP context and connect string are specific to T-Mobile. If a
different SIM card is used, change /controller/attach_python.py and create a
new provider script and a new chat script.  

3) The startup script runs pppd to connect the line.  The GPRS shield must be 
turned on right after the device is started.  The GPRS can be turned on by
software but only if the switch is soldered off.  For the MVP demo we can turn
it on via the switch.

4) The startup script is in /etc/init.d/startup.sh

