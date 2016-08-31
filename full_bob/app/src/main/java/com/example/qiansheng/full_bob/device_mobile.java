package com.example.qiansheng.full_bob;

/**
 * Created by qiansheng on 8/14/16.
 */
public class device_mobile {
    public String device_ID="o";
    public Integer type = 0;
    public String mobile_num="o";
    device_mobile(String email, int type, String password) {
        this.device_ID = email;
        this.type = type;
        this.mobile_num = password;
    }
    public void setDevID(String dev_ID) {
        this.device_ID = dev_ID;
    }
    public void setMobNum(String mob_num) {
        this.mobile_num = mob_num;
    }
    public void setType(int type) {
        this.type = type;
    }

    public String getDevID() {
        return device_ID;
    }
    public int getType() {
        return type;
    }
    public String getMobNum() {
        return mobile_num;
    }
}
