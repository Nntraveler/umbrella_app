package com.example.myapplication.database;

import org.litepal.crud.LitePalSupport;

public class Device extends LitePalSupport {

    private int id;

    private String deviceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}

