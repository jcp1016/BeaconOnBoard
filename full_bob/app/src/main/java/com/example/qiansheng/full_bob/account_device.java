package com.example.qiansheng.full_bob;

/**
 * Created by qiansheng on 8/14/16.
 */
public class account_device {
    public String account="o";
    public String device_ID="o";
    account_device(String email, String device_ID) {
        this.device_ID = device_ID;
        this.account=email;
    }
    public void setDevID(String dev_ID) {
        this.device_ID = dev_ID;
    }
    public void setAccount(String email) {
        this.account = email;
    }

    public String getDevID() {
        return device_ID;
    }
    public String getAccount() {
        return account;
    }
}
