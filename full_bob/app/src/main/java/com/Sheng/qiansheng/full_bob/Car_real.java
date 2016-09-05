package com.Sheng.qiansheng.full_bob;

/**
 * Created by qiansheng on 8/27/16.
 */

public class Car_real {
    public String device_ID="o";
    public long start_time=0;
    public float temperature=0;
    Car_real(String device_ID, long time, float temperature) {
        this.device_ID = device_ID;
        this.start_time=time;
        this.temperature=temperature;
    }
    public void setDevID(String dev_ID) {
        this.device_ID = dev_ID;
    }
    public void setStartTime(long time) {
        this.start_time = time;
    }
    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getDevID() {
        return device_ID;
    }
    public long getStartTime() {
        return start_time;
    }
    public float getTemperature() { return temperature; }
}
