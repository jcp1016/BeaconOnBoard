package com.Sheng.qiansheng.full_bob;

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

/**
 * Created by qiansheng on 7/14/16.
 */
public class SignUp extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.Sheng.qiansheng.full_bob.R.layout.sign_up);

        Button button_signup = (Button) findViewById(com.Sheng.qiansheng.full_bob.R.id.button_signup_action);
        assert button_signup != null;

        button_signup.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mEmail = (EditText) findViewById(com.Sheng.qiansheng.full_bob.R.id.editText_dev_id);
                EditText mPass = (EditText) findViewById(com.Sheng.qiansheng.full_bob.R.id.editText_mobile_num);
                EditText mConfirm = (EditText) findViewById(com.Sheng.qiansheng.full_bob.R.id.editText_confirm);
                String email = mEmail.getText().toString();
                String password = mPass.getText().toString();
                String confirm = mConfirm.getText().toString();
                if (!password.equals(confirm)){
                    sendNotification("Password and confirm don't match.");
                }else{
                    accountDetail new_account = new accountDetail(email,password);
                    new DynamoDBManagerTask()
                            .execute(new_account);
                }
            }
        });

    }
//    @Override
//    protected void onResume(){
//        super.onResume();
//        // put your code here...
//        System.out.println("==========test==========");
//        System.out.println(ImagePart.device_string);
//        if(ImagePart.device_string==null) {
//            System.out.println("==========test==========");
//        }else{
//            System.out.println("==========work?==========");
//            EditText mEmail = (EditText) findViewById(R.id.editText_dev_id);
//            mEmail.setText(ImagePart.device_string,TextView.BufferType.EDITABLE);
//        }
//    }
    private class DynamoDBManagerTask extends
            AsyncTask<accountDetail, Void, String> {

        protected String doInBackground(
                accountDetail... accountDetails) {
            String email = accountDetails[0].email;
            String password = accountDetails[0].password;
            DynamoDBManager.Accounts correctOne = DynamoDBManager.getAccount(email);
            if (correctOne==null){
                DynamoDBManager.insertAccount(email,password);
                return "Successfully";
            }else{
                return "Email has been registered.";
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
    public void sendNotification(String contentText) {
        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(com.Sheng.qiansheng.full_bob.R.layout.popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT);
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
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }
}
