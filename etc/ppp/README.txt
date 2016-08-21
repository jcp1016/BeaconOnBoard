1) In order to use GPRS we first had to rebuild the Yocto Linux kernel with
ppp enabled.  Rebuilding the kernel takes several days.

2) The GPRS shield has an activated SIM card that currently uses a T-Mobile
plan.  The chat script is specific to T-Mobile.  If a different SIM card is 
used, a new chat script will be needed.

3) The startup script runs pppd to connect the line.  The GPRS shield must be 
turned on before the device is booted.  The GPRS can be turned on by software 
but only if the switch is soldered off.  For the MVP demo we are turning it 
on manually.

4) The startup script is in /etc/init.d/

