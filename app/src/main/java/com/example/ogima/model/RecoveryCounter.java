package com.example.ogima.model;

import java.io.Serializable;

public class RecoveryCounter implements Serializable {
    private String userId;
    private int counter;
    private long timeStampValidity;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public long getTimeStampValidity() {
        return timeStampValidity;
    }

    public void setTimeStampValidity(long timeStampValidity) {
        this.timeStampValidity = timeStampValidity;
    }
}
