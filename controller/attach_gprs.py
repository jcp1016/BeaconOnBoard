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
import time, sys, signal, atexit, os
import pyupm_grovegprs as modemObj

OK = "OK"

def ExitHandler():
    print "Exiting"
    sys.exit(0)

def SendCommand(cmd, t):
    # commands must be terminated with a carriage return
    cmd += "\r"
    modem.writeDataStr(cmd)

    # wait t seconds
    t *= 1000
    while modem.dataAvailable(t):
        print modem.readDataStr(1024)
    return 


if __name__ == '__main__':

    modem = modemObj.GroveGPRS(0)

    # 19200 is the default but it can be changed
    modem.setBaudRate(19200)

    # Register exit handlers
    atexit.register(ExitHandler)

    SendCommand("ATZ",1)

    # Deactivate the PDP context if it exists
    SendCommand("AT+CGACT=0,1",1)

    # Detach the GPRS if it is attached
    SendCommand("AT+CGATT=0",1)

    SendCommand("AT+CMEE=2",1)

    # Is the device ready?
    SendCommand("AT+CPIN?",1)

    # Set PIN
    #SendCommand('AT+CPIN="8811"',1)

    # Is the device registered?
    SendCommand("AT+CGREG?",1)

    # Does the SIM card match the provider?
    #SendCommand("AT+COPS=1,2,310260,0",1)

    SendCommand("AT+COPS?",1)

    # Check the line quality
    SendCommand("AT+CSQ",1)

    # Define a PDP context with IP connection, ID is 1
    SendCommand('AT+CGDCONT=1,"","fast.t-mobile.com"',2);
    #SendCommand('AT+CGDCONT=1,"IP","internet.t-mobile"',2);

    # Attach to GPRS network
    SendCommand("AT+CGATT=1",1)

    # Activate the PDP context
    SendCommand("AT+CGACT=1,1",1)

    # Query the active PDP contexts
    SendCommand("AT+CGACT?",1)

    # Connect to ISP
    SendCommand("ATD*99***1#",1)
    #SendCommand('AT+CGDATA="PPP",1',2)

    print "Now run pppd call provider"
