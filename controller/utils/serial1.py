#-------------------------------------------------------------
# attach_gprs.py
#
# Desc:  Defines PDP context and connects GPRS to the ISP.
#        This needs to run each time the modem is restarted.
#        It does not need to run if the device reboots because the 
#        GPRS has its own backup battery.
#       
# Date:  August 2016
#-------------------------------------------------------------
import pyupm_grovegprs as modemObj
import sys

OK = "OK"

def ExitHandler():
    print "Exiting"
    sys.exit(0)

if __name__ == '__main__':

    cmd = sys.argv[1]

    modem = modemObj.GroveGPRS(0)
    modem.setBaudRate(19200)

    atexit.register(ExitHandler)

    cmd += "\r"
    t *= 1000

    modem.writeDataStr(cmd)
    while modem.dataAvailable(1000):
        print modem.readDataStr(1024)
