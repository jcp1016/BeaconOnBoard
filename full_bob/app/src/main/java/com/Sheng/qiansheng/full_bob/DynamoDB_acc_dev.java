package com.Sheng.qiansheng.full_bob;

import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by qiansheng on 8/15/16.
 */
public class DynamoDB_acc_dev {
    private static final String TAG = "DynamoDB_acc_dev";

    public static void insertBind(String dev_num, Integer end_type, String end_point) {
        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Dev_Endpoint bind_combination = new Dev_Endpoint();
            bind_combination.setDevNum(dev_num);
            bind_combination.setType(end_type);
            bind_combination.setEndPoint(end_point);
            mapper.save(bind_combination);
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }
    public static Dev_Endpoint getAccount(String dev_num, String end_point) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Dev_Endpoint one_combination = mapper.load(Dev_Endpoint.class,
                    dev_num, end_point);

            return one_combination;

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static void updateBind(Dev_Endpoint updateBind) {

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
    public static void deleteBind(Dev_Endpoint deleteBind) {

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

    @DynamoDBTable(tableName = Constants.ENDPOINT_TABLE_NAME)
    public static class Dev_Endpoint {
        private String DevNum;
        private Integer End_Type;  // '0' -> SMS, '1' -> Application
        private String EndPoint;
        @DynamoDBHashKey(attributeName = "DevNum")
        public String getDevNum() {
            return DevNum;
        }

        public void setDevNum(String dev_num) {
            this.DevNum = dev_num;
        }

        @DynamoDBAttribute(attributeName = "End_Type")
        public Integer getType() {
            return End_Type;
        }

        public void setType(Integer myType) {
            this.End_Type = myType;
        }

        @DynamoDBRangeKey(attributeName = "EndPoint")
        public String getEndPoint() {
            return EndPoint;
        }

        public void setEndPoint(String endPoint) {
            this.EndPoint = endPoint;
        }
    }
}