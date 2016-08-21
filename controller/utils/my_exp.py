import mtaUpdates

with open('./key.txt', 'rb') as keyfile:
    APIKEY = keyfile.read().rstrip('\n')
    keyfile.close()
a = mtaUpdates.mtaUpdates(APIKEY)
b = a.getTripUpdates()[1]

print b.tripId
print b.routeId
print b.startDate 
print b.direction
print b.vehicleData.currentStopNumber
print b.vehicleData.currentStopId
print b.vehicleData.timestamp
print b.vehicleData.currentStopStatus
print b.futureStops
print '1'
#print a.tripUpdates

