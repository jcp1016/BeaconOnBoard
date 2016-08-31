package com.example.qiansheng.full_bob;

/**
 * Created by qiansheng on 7/8/16.
 */

import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class DynamoDBManager {

    private static final String TAG = "DynamoDBManager";

//    public static void createTable() {
//
//        Log.d(TAG, "Create table called");
//
//        AmazonDynamoDBClient ddb = MainActivity.clientManager
//                .ddb();
//
//        KeySchemaElement kse = new KeySchemaElement().withAttributeName(
//                "userNo").withKeyType(KeyType.HASH);
//        AttributeDefinition ad = new AttributeDefinition().withAttributeName(
//                "userNo").withAttributeType(ScalarAttributeType.N);
//        ProvisionedThroughput pt = new ProvisionedThroughput()
//                .withReadCapacityUnits(10l).withWriteCapacityUnits(5l);
//
//        CreateTableRequest request = new CreateTableRequest()
//                .withTableName(Constants.TEST_TABLE_NAME)
//                .withKeySchema(kse).withAttributeDefinitions(ad)
//                .withProvisionedThroughput(pt);
//
//        try {
//            Log.d(TAG, "Sending Create table request");
//            ddb.createTable(request);
//            Log.d(TAG, "Create request response successfully recieved");
//        } catch (AmazonServiceException ex) {
//            Log.e(TAG, "Error sending create table request", ex);
//            MainActivity.clientManager
//                    .wipeCredentialsOnAuthError(ex);
//        }
//    }

    public static void insertAccount(String email, String password) {
        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Accounts myAccount = new Accounts();
            myAccount.setEmail(email);
            myAccount.setPassword(password);
            mapper.save(myAccount);
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }
    public static Accounts getAccount(String Email) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            Accounts account = mapper.load(Accounts.class,
                    Email);

            return account;

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static void updateAccount(Accounts updateAccount) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.save(updateAccount);

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Deletes the specified user and all of its attribute/value pairs.
     */
    public static void deleteAccount(Accounts deleteAccount) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.delete(deleteAccount);

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }
    @DynamoDBTable(tableName = Constants.TEST_TABLE_NAME)
    public static class Accounts {
        private String Email;
        private String Password;
        @DynamoDBHashKey(attributeName = "Email")
        public String getEmail() {
            return Email;
        }

        public void setEmail(String Email) {
            this.Email = Email;
        }

        @DynamoDBAttribute(attributeName = "Password")
        public String getPassword() {
            return Password;
        }

        public void setPassword(String Password) {
            this.Password = Password;
        }
    }
}