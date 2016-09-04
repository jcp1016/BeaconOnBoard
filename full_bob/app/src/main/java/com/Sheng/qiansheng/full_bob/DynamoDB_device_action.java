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
public class DynamoDB_device_action {
    private static final String TAG = "DynamoDB_device_action";
    public static void insertBind(String dev_num, int active) {
        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Device_act bind_combination = new Device_act();
            bind_combination.setDevNum(dev_num);
            bind_combination.setActive(active);
            mapper.save(bind_combination);
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }
    public static Device_act getAccount(String dev_num) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Device_act one_combination = mapper.load(Device_act.class,
                    dev_num);

            return one_combination;

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static void updateBind(Device_act updateBind) {

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
    public static void deleteBind(Device_act deleteBind) {

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

    @DynamoDBTable(tableName = Constants.DEVICE_ACTION_TABLE_NAME)
    public static class Device_act {
        private String DevNum;
        private int active;  // '0' -> deactive, '1' -> active
        @DynamoDBHashKey(attributeName = "DevNum")
        public String getDevNum() {
            return DevNum;
        }

        public void setDevNum(String dev_num) {
            this.DevNum = dev_num;
        }

        @DynamoDBAttribute(attributeName = "active")
        public int getActive() {
            return active;
        }

        public void setActive(int active) {
            this.active = active;
        }

    }
}