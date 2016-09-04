package com.Sheng.qiansheng.full_bob;

        import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by qiansheng on 8/15/16.
 */
public class DynamoDB_account_device {
    private static final String TAG = "DynamoDB_account_device";

    public static void insertBind( String email, String dev_num) {
        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Account_Dev bind_combination = new Account_Dev();
            bind_combination.setDevNum(dev_num);
            bind_combination.setAccount(email);
            mapper.save(bind_combination);
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }
    public static Account_Dev getDev_num(String email) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Account_Dev one_combination = mapper.load(Account_Dev.class,
                    email);

            return one_combination;

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static Account_Dev getAccount(String email,String dev_num) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Account_Dev one_combination = mapper.load(Account_Dev.class,
                    email, dev_num);

            return one_combination;

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static void updateBind(Account_Dev updateBind) {

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
    public static void deleteBind(Account_Dev deleteBind) {

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

    @DynamoDBTable(tableName = Constants.BIND_TABLE_NAME)
    public static class Account_Dev {
        private String Account;
        private String DevNum;

        @DynamoDBHashKey(attributeName = "account")
        public String getAccount() {
            return Account;
        }

        public void setAccount(String email) {
            this.Account = email;
        }

        @DynamoDBRangeKey(attributeName = "deviceID")
        public String getDevNum() {
            return DevNum;
        }

        public void setDevNum(String dev_num) {
            this.DevNum = dev_num;
        }


    }
}