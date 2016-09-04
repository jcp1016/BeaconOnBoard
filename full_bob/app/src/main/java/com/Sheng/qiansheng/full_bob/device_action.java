package com.Sheng.qiansheng.full_bob;

/**
 * Created by qiansheng on 8/27/16.
 */

public class device_action {
    public String device_ID="o";
    public int active=0;
    device_action(String device_ID, int active) {
        this.device_ID = device_ID;
        this.active=active;
    }
    public void setDevID(String dev_ID) {
        this.device_ID = dev_ID;
    }
    public void setActive(int active) {
        this.active = active;
    }

    public String getDevID() {
        return device_ID;
    }
    public long getActive() {
        return active;
    }

}