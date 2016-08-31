package com.example.qiansheng.full_bob;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.CreateTopicResult;

/**
 * Created by qiansheng on 8/14/16.
 */
public class bind_device extends Activity {
    static AmazonSNS snsClient = MainActivity.clientManager.sns();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bind_device);
        Button button_bind = (Button) findViewById(R.id.button_bind_action);
        assert button_bind != null;
        button_bind.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mDevID = (EditText) findViewById(R.id.editText_dev_id);
                EditText mMobNum = (EditText) findViewById(R.id.editText_mobile_num);
                EditText mConfirm = (EditText) findViewById(R.id.editText_confirm);
                String DevID = mDevID.getText().toString();
                String MobNum = mMobNum.getText().toString();
                String confirm = mConfirm.getText().toString();
                if (!MobNum.equals(confirm)){
                    sendNotification("Mobile number and confirm don't match.");
                }else{
                    device_mobile new_account = new device_mobile(DevID,0,MobNum);
                    new DynamoDB_acc_devTask()
                            .execute(new_account);
                }
            }
        });

    }
    private class DynamoDB_acc_devTask extends
            AsyncTask<device_mobile, Void, String> {

        protected String doInBackground(
                device_mobile... device_mobiles) {
            String DevID = device_mobiles[0].device_ID;
            int type = device_mobiles[0].type;
            String MobNum = device_mobiles[0].mobile_num;
            CreateTopicResult topic = snsClient.createTopic(DevID);
            System.out.println(topic);
            if (MainActivity.token!=null){
                DynamoDB_acc_dev.Dev_Endpoint correctOne2 = DynamoDB_acc_dev.getAccount(DevID,MainActivity.token);
                if (correctOne2==null){
                    DynamoDB_acc_dev.insertBind(DevID,1,MainActivity.token);
                    System.out.println("testSNS sub");
                    CreatePlatformEndpointRequest cpeReq =
                            new CreatePlatformEndpointRequest()
                                    .withPlatformApplicationArn("arn:aws:sns:us-east-1:480370410475:app/GCM/bobo")
                                    .withToken(MainActivity.token);
                    CreatePlatformEndpointResult cpeRes = snsClient
                            .createPlatformEndpoint(cpeReq);
                    String endpointArn = cpeRes.getEndpointArn();
                    snsClient.subscribe(topic.getTopicArn(),"application",endpointArn);
                }
            }
            DynamoDB_account_device.Account_Dev correctOne3 = DynamoDB_account_device.getAccount(MainActivity.my_account,DevID);
            if (correctOne3==null){
                DynamoDB_account_device.insertBind(MainActivity.my_account,DevID);
            }
            DynamoDB_acc_dev.Dev_Endpoint correctOne = DynamoDB_acc_dev.getAccount(DevID,MobNum);
            if (correctOne==null){
                DynamoDB_acc_dev.insertBind(DevID,type,MobNum);
                String phone_num = "1-";
                phone_num = phone_num.concat(MobNum.substring(0,3));
                phone_num = phone_num.concat("-");
                phone_num = phone_num.concat(MobNum.substring(3,6));
                phone_num = phone_num.concat("-");
                phone_num = phone_num.concat(MobNum.substring(6,10));
                snsClient.subscribe(topic.getTopicArn(),"sms",phone_num);
                return "Successfully";
            }else{
                return "Already binded.";
            }

        }
        protected void onPostExecute(String result){
            if(result.equals("Successfully")){
                Intent act3 = new Intent(getApplicationContext(), ImagePart.class);
                startActivity(act3);
            }else{
                sendNotification(result);
            }
        }
    }
    public void sendNotification(String contentText) {
        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT);
        //TextView pop_text = (TextView) findViewById(popupView.id.pop_text);
        //pop_text.setText(contentText);
        Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);
        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
            }
        });
        TextView pop_text = (TextView)popupView.findViewById(R.id.pop_text);
        pop_text.setText(contentText);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }
}
