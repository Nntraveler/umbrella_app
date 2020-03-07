package com.example.myapplication.database;

import org.litepal.crud.LitePalSupport;

public class Device extends LitePalSupport {

    private int id;

    private String deviceName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}

