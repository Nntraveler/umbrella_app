package com.example.myapplication.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond_text")
    public String conditionDay;

    @SerializedName("fl")
    public String bodyFeelingTemperature;

    @SerializedName("hum")
    public String humidity;

    @SerializedName("pres")
    public String atmosphericPressure;

    @SerializedName("vis")
    public String visibility;
}
