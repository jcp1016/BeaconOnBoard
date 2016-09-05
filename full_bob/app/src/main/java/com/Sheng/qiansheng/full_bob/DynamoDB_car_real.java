package com.Sheng.qiansheng.full_bob;

import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by qiansheng on 8/15/16.
 */
public class DynamoDB_car_real {
    private static final String TAG = "DynamoDB_car_real";
    public static void insertBind(String dev_num, long start_time, float temperature) {
        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Car_real_Inside bind_combination = new Car_real_Inside();
            bind_combination.setDevNum(dev_num);
            bind_combination.setStart_time(start_time);
            bind_combination.setTemperature(temperature);
            mapper.save(bind_combination);
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }
    public static Car_real_Inside getAccount(String dev_num) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Car_real_Inside one_combination = mapper.load(Car_real_Inside.class,
                    dev_num);

            return one_combination;

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static void updateBind(Car_real_Inside updateBind) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.save(updateBind);

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Deletes the specified user and all of its attribute/value pairs.
     */
    public static void deleteBind(Car_real_Inside deleteBind) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.delete(deleteBind);

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    @DynamoDBTable(tableName = Constants.CAR_REAL_TABLE_NAME)
    public static class Car_real_Inside {
        private String DevNum;
        private long Start_time;  // '0' -> SMS, '1' -> Application
        private float Temperature;
        @DynamoDBHashKey(attributeName = "DevNum")
        public String getDevNum() {
            return DevNum;
        }

        public void setDevNum(String dev_num) {
            this.DevNum = dev_num;
        }

        @DynamoDBAttribute(attributeName = "start_time")
        public long getStart_time() {
            return Start_time;
        }

        public void setStart_time(long time) {
            this.Start_time = time;
        }

        @DynamoDBAttribute(attributeName = "temperature")
        public float getTemperature() {
            return Temperature;
        }

        public void setTemperature(float temperature) {
            this.Temperature = temperature;
        }
    }
}