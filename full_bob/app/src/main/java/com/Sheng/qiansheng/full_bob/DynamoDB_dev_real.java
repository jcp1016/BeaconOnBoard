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
public class DynamoDB_dev_real {
    private static final String TAG = "DynamoDB_dev_real";
    public static void insertBind(String dev_num, int upload, int finished) {
        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Dev_real_Inside bind_combination = new Dev_real_Inside();
            bind_combination.setDevNum(dev_num);
            bind_combination.setUpload(upload);
            bind_combination.setFinished(finished);
            mapper.save(bind_combination);
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }
    public static Dev_real_Inside getAccount(String dev_num) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Dev_real_Inside one_combination = mapper.load(Dev_real_Inside.class,
                    dev_num);

            return one_combination;

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static void updateBind(Dev_real_Inside updateBind) {

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
    public static void deleteBind(Dev_real_Inside deleteBind) {

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

    @DynamoDBTable(tableName = Constants.DEV_REAL_TABLE_NAME)
    public static class Dev_real_Inside {
        private String DevNum;
        private int Upload;  // '0' -> inactive, '1' -> active
        private int Finished; // '0' -> unfinished, '1' ->finished
        @DynamoDBHashKey(attributeName = "DevNum")
        public String getDevNum() {
            return DevNum;
        }

        public void setDevNum(String dev_num) {
            this.DevNum = dev_num;
        }

        @DynamoDBAttribute(attributeName = "Upload")
        public int getUpload() {
            return Upload;
        }

        public void setUpload(int upload) {
            this.Upload = upload;
        }

        @DynamoDBAttribute(attributeName = "Finished")
        public int getFinished() {
            return Finished;
        }

        public void setFinished(int finished) {
            this.Finished = finished;
        }
    }
}