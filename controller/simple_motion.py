import cv2
import time
from datetime import datetime
import sys
import numpy

def diffImg(t0, t1, t2):
  d1 = cv2.absdiff(t2, t1)
  # d2 = cv2.absdiff(t1, t0)
  # d3 = cv2.bitwise_and(d1, d2)
  # res = cv2.blur(d3, (5,5))
  res = cv2.blur(d1, (5,5))
  res = cv2.morphologyEx(res, cv2.MORPH_OPEN, None)
  res = cv2.morphologyEx(res, cv2.MORPH_CLOSE, None)
  res = cv2.threshold(res, 10, 255, cv2.THRESH_BINARY_INV)
  #return cv2.addWeighted(d1,0.5,d2,0.5,0)
  return res[1]

def somethingHasMoved(frame, threshold):
    nb=0 #Will hold the number of black pixels
    
    # for x in range(frame.shape[0]): #Iterate the hole image
    #     for y in range(frame.shape[1]):
    #         if frame[x,y] == 0.0: #If the pixel is black keep it
    #             nb += 1
    
    nb=len(numpy.where(frame == 0)[0])
    avg = (nb*100.0)/(frame.shape[0]*frame.shape[1]) #Calculate the average of black pixel in the image

    if avg > threshold:#If over the ceiling trigger the alarm
        return True
    else:
        return False

cam = cv2.VideoCapture(0)
s, img = cam.read()

#winName = "Movement Indicator"
#cv2.namedWindow(winName, cv2.CV_WINDOW_AUTOSIZE)
started = time.time()
# Read three images first:
t_minus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
t = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
t_plus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)

try:
  while True:
    instant = time.time()
    time.sleep(0.001) 
    frame = diffImg(t_minus, t, t_plus)
    #cv2.imshow(winName, frame)
    # Read next image
    t_minus = t
    t = t_plus
    t_plus = cv2.cvtColor(cam.read()[1], cv2.COLOR_RGB2GRAY)
    
    if somethingHasMoved(frame, 5):#Wait 5 second after the webcam start for luminosity adjusting etc..
      if instant > started +5:
        print datetime.now().strftime("%b %d, %H:%M:%S"), "Something is moving !"

except KeyboardInterrupt:
  print "Goodbye"
  sys.exit()
