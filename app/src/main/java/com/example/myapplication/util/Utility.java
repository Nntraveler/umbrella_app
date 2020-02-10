package com.example.myapplication.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.myapplication.database.City;
import com.example.myapplication.database.County;
import com.example.myapplication.database.Province;
import com.example.myapplication.gson.Basic;
import com.example.myapplication.gson.Forecast;
import com.example.myapplication.gson.Now;
import com.example.myapplication.gson.Suggestion;
import com.example.myapplication.gson.Update;
import com.example.myapplication.gson.Weather;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Utility {
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); ++i) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); ++i) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); ++i) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void handleWeatherNow(String response, Weather resultWeather) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            JSONObject weatherContent = jsonArray.getJSONObject(0);
            String update = weatherContent.getJSONObject("update").toString();
            String basic = weatherContent.getJSONObject("basic").toString();
            String now = weatherContent.getJSONObject("now").toString();
            Gson gson = new Gson();
            resultWeather.update = gson.fromJson(update, Update.class);
            resultWeather.basic = gson.fromJson(basic, Basic.class);
            resultWeather.now = gson.fromJson(now, Now.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleWeatherForecast(String response, Weather resultWeather) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            JSONObject weatherContent = jsonArray.getJSONObject(0);
            String forecasts = weatherContent.getJSONArray("daily_forecast").toString();
            Gson gson = new Gson();
            resultWeather.forecastList = gson.fromJson(forecasts, new TypeToken<List<Forecast>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleWeatherSuggestions(String response, Weather resultWeather) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            JSONObject weatherContent = jsonArray.getJSONObject(0);
            JSONArray suggestions = weatherContent.getJSONArray("lifestyle");
            resultWeather.suggestion = new Suggestion();
            for (int i = 0; i < suggestions.length(); ++i) {
                JSONObject suggestion = suggestions.getJSONObject(i);
                switch (suggestion.getString("type")) {
                    case "comf":
                        resultWeather.suggestion.comfort = suggestion.getString("txt");
                        break;
                    case "sport":
                        resultWeather.suggestion.sport = suggestion.getString("txt");
                        break;
                    case "cw":
                        resultWeather.suggestion.carWash = suggestion.getString("txt");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
