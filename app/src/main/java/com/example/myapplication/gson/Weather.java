package com.example.myapplication.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Weather {

    public String status;

    public Basic basic;

    public Update update;

    public Now now;

    @SerializedName("lifestyle")
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList = new ArrayList<>();
}
