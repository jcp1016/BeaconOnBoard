#!/usr/bin/python

import mraa
import time
import math

motion_pin_number = 6
temp_pin_number = 7
led_pin_number = 8

motion = mraa.Gpio(motion_pin_number)
motion.dir(mraa.DIR_IN)

led = mraa.Gpio(led_pin_number)
led.dir(mraa.DIR_OUT)

led.write(1)
time.sleep(1)
led.write(0)

B = 4275 # Value of the thermistor

print "Press Ctrl+C to escape..."
i = 0
while True:
    try:
        if (motion.read()):
            i += 1
            print 'Motion detected ' + str(i)
            led.write(1)
            time.sleep(0.05)
            led.write(0)

            # Display temperature
            tempSensor = mraa.Aio(1)
            temp = tempSensor.read()
            R = 1023.0 / temp - 1.0
            R *= 100000.0

            # Convert to Celsius according to sensor datasheet
            tempC = 1.0 / (math.log( R/100000.0 ) / B+1 / 298.15) - 273.15
            tempF = tempC * 9.0/5 + 32

            # Write temperature to stdout
            print "Current temperature: {:3.1f} F | {:3.1f} C".format(tempF, tempC)
    except KeyboardInterrupt:
        exit
