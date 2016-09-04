package com.Sheng.qiansheng.full_bob;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

//import android.support.v7.widget.Toolbar.LayoutParams;
//import android.support.v7.app.NotificationCompat;

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    public static AmazonClientManager clientManager = null;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    public static String token = null;
    public static String my_account = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clientManager = new AmazonClientManager(this);
        AmazonDynamoDBClient ddb = clientManager.ddb();

        setContentView(com.Sheng.qiansheng.full_bob.R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(com.Sheng.qiansheng.full_bob.R.id.toolbar);
        new Thread(new Runnable() {
            public void run() {
                InstanceID instanceID = InstanceID.getInstance(getApplicationContext());

                String scope = "GCM"; // e.g. communicating using GCM, but you can use any
                // URL-safe characters up to a maximum of 1000, or
                // you can also leave it blank.
                try {
                    token = instanceID.getToken(getString(com.Sheng.qiansheng.full_bob.R.string.gcm_defaultSenderId),
                            "GCM", null);
                    System.out.println(token);
                    System.out.println(getString(com.Sheng.qiansheng.full_bob.R.string.gcm_defaultSenderId));
                    System.out.println("here");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        // Registering BroadcastReceiver
        //setSupportActionBar(toolbar);
        final CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
//                "480370410475",
                "us-east-1:e09774a3-f3ab-4eab-bf40-3ba447537627", // Identity Pool ID
//                "arn:aws:iam::480370410475:role/Cognito_BOBOUnauth_Role",
//                "arn:aws:iam::480370410475:role/Cognito_BOBOAuth_Role",
                Regions.US_EAST_1 // Region
        );

        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        //System.out.println("hahaha");

        Button button_signup = (Button) findViewById(com.Sheng.qiansheng.full_bob.R.id.button_signup);
        final Intent act2 = new Intent(this,SignUp.class);
        assert button_signup != null;

        OnClickListener signup_listener = new OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(act2);

            }
        };
//        System.out.println(signup_listener);
        button_signup.setOnClickListener(signup_listener);
        final Button button = (Button) findViewById(com.Sheng.qiansheng.full_bob.R.id.button_signin);
        EditText mEdit = (EditText) findViewById(com.Sheng.qiansheng.full_bob.R.id.editText_dev_id);

        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText mEmail = (EditText) findViewById(com.Sheng.qiansheng.full_bob.R.id.editText_dev_id);
                EditText mPass = (EditText) findViewById(com.Sheng.qiansheng.full_bob.R.id.editText_mobile_num);
                accountDetail new_account = new accountDetail(mEmail.getText().toString(),mPass.getText().toString());
                new DynamoDBManagerTask().execute(new_account);
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.Sheng.qiansheng.full_bob/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.Sheng.qiansheng.full_bob/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class DynamoDBManagerTask extends
            AsyncTask<accountDetail, Void, String> {

        protected String doInBackground(
                accountDetail... accountDetails) {
            String email = accountDetails[0].email;
            String password = accountDetails[0].password;
            DynamoDBManager.Accounts correctOne = DynamoDBManager.getAccount(email);
            System.out.println(correctOne);
            String result;
            if (correctOne==null){
                result = "Email is not registered.";
                return result;
            }
            if(correctOne.getPassword().equals(password)){
                my_account = email;
                result = "Login successfully";
            }else{
                result = "Email and/or Password is wrong.";
            }
            return result;
        }
        protected void onPostExecute(String result) {
            if(result.equals("Login successfully")) {
                Intent act3 = new Intent(getApplicationContext(), ImagePart.class);
                startActivity(act3);
            }else{
                sendNotification(result);
            }
        }
    }
    private static class accountDetail{
        public String email="o";
        public String password="o";
        accountDetail(String email, String password) {
            this.email = email;
            this.password = password;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getEmail() {
            return email;
        }
        public String getPassword() {
            return password;
        }
    }
    public void onBackPressed() {
        //do nothing
    }
    public void sendNotification(String contentText) {
        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(com.Sheng.qiansheng.full_bob.R.layout.popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        //TextView pop_text = (TextView) findViewById(popupView.id.pop_text);
        //pop_text.setText(contentText);
        Button btnDismiss = (Button)popupView.findViewById(com.Sheng.qiansheng.full_bob.R.id.dismiss);
        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
            }
        });
        TextView pop_text = (TextView)popupView.findViewById(com.Sheng.qiansheng.full_bob.R.id.pop_text);
        pop_text.setText(contentText);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);;
    }

}
