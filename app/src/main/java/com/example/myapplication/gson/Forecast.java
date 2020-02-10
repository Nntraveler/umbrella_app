package com.example.myapplication.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    public String date;

    @SerializedName("tmp_max")
    public String tempMax;

    @SerializedName("tmp_min")
    public String tempMin;

    @SerializedName("cond_txt_d")
    public String conditionDay;
}
