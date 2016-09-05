package com.Sheng.qiansheng.full_bob;

/**
 * Created by qiansheng on 8/27/16.
 */

public class Dev_real {
    public String device_ID="o";
    public int upload=0;
    public int finished=0;
    Dev_real(String device_ID, int upload, int finished) {
        this.device_ID = device_ID;
        this.upload=upload;
        this.finished=finished;
    }
    public void setDevID(String dev_ID) {
        this.device_ID = dev_ID;
    }
    public void setUpload(int upload) {
        this.upload = upload;
    }
    public void setFinished(int finished) {
        this.finished = finished;
    }

    public String getDevID() {
        return device_ID;
    }
    public long getUpload() {
        return upload;
    }
    public float getFinished() { return finished; }
}