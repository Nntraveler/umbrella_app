package com.example.myapplication.database;

import org.litepal.crud.LitePalSupport;

public class Device extends LitePalSupport {

    private int id;

    private String deviceMACaddress;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceMACaddress() {
        return deviceMACaddress;
    }

    public void setDeviceMACaddress(String deviceMACaddress) {
        this.deviceMACaddress = deviceMACaddress;
    }
}
