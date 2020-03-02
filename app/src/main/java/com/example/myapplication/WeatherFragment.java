package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.gson.Forecast;
import com.example.myapplication.gson.Suggestion;
import com.example.myapplication.gson.Weather;
import com.example.myapplication.util.HttpUtil;
import com.example.myapplication.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherFragment extends Fragment {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView bodyFeelingTemperatureText;

    private TextView humidityText;

    private TextView pressureText;

    private TextView visibilityText;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    boolean httpRequestSucceed;

    private Weather weather = new Weather();

    private Activity activity;

    //fragment初始化
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        initAllProperty(view);
        //检查本地是否有已存入的天气信息
        SharedPreferences prefs = activity.getSharedPreferences("weather",
                Context.MODE_PRIVATE);
        String weatherNow = prefs.getString("WEATHER_NOW", null);
        String weatherForecast = prefs.getString("WEATHER_FORECAST", null);
        String weatherSuggestions = prefs.getString("WEATHER_SUGGESTIONS", null);
        String weatherId = prefs.getString("WEATHER_ID", null);
        //若本地已有已存入的天气信息则直接加载界面
        if (weatherNow != null && weatherForecast != null && weatherSuggestions != null) {
            Utility.handleWeatherNow(weatherNow, weather);
            Utility.handleWeatherForecast(weatherForecast, weather);
            Utility.handleWeatherSuggestions(weatherSuggestions, weather);
            showWeatherNow(weather);
            showWeatherForecast(weather.forecastList);
            showWeatherSuggestions(weather.suggestion);
        } else if (weatherId != null) { //若本地沒有已存入的天气信息，则依次查询
            if (weatherNow == null) {
                requestWeatherNow(weatherId);
            }
            if (weatherForecast == null) {
                requestWeatherForecast(weatherId);
            }
            if (weatherSuggestions == null) {
                requestWeatherSuggestions(weatherId);
            }
        }
        return view;
    }

    //初始化所有view
    private void initAllProperty(View view) {
        weatherLayout = view.findViewById(R.id.weather_frag_scroll_view);
        titleCity = view.findViewById(R.id.weather_frag_title_city);
        titleUpdateTime = view.findViewById(R.id.weather_frag_title_update_time);
        degreeText = view.findViewById(R.id.weather_frag_now_degree);
        weatherInfoText = view.findViewById(R.id.weather_frag_now_info);
        forecastLayout = view.findViewById(R.id.weather_frag_forecast_layout);
        bodyFeelingTemperatureText = view.findViewById(R.id.weather_frag_info_fl);
        humidityText = view.findViewById(R.id.weather_frag_info_hum);
        pressureText = view.findViewById(R.id.weather_frag_info_pres);
        visibilityText = view.findViewById(R.id.weather_frag_info_vis);
        comfortText = view.findViewById(R.id.weather_suggestion_comfort);
        carWashText = view.findViewById(R.id.weather_suggestion_car_wash);
        sportText = view.findViewById(R.id.weather_suggestion_sport);
    }

    //因和风天气api无法同时查询当前天气，天气预报和生活建议，所以需要分开查询，此处为查询当前天气
    public void requestWeatherNow(final String weatherId) {
        String weatherUrl =
                "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key" +
                        "=1872edf195e5429f88b0c48e723f36b0";
        //发送请求
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            //若查询失败则提示
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //若查询成功则通过sharedPreference保存到本地然后加载界面
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                Utility.handleWeatherNow(responseText, weather);
                SharedPreferences.Editor editor = activity.getSharedPreferences(
                        "weather", Context.MODE_MULTI_PROCESS).edit();
                editor.putString("WEATHER_NOW", responseText);
                editor.apply();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather.now != null) {
                            showWeatherNow(weather);
                        } else {
                            Toast.makeText(getActivity(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //查询进三日天气预报，若失败则提示，若成功则保存到本地并加载界面
    public void requestWeatherForecast(final String weatherId) {
        String weatherUrl =
                "https://free-api.heweather.net/s6/weather/forecast?location=" + weatherId +
                        "&key=1872edf195e5429f88b0c48e723f36b0";
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                SharedPreferences.Editor editor = activity.getSharedPreferences(
                        "weather", Context.MODE_MULTI_PROCESS).edit();
                editor.putString("WEATHER_FORECAST", responseText);
                editor.apply();
                Utility.handleWeatherForecast(responseText, weather);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather.forecastList.size() > 0) {
                            showWeatherForecast(weather.forecastList);
                        } else {
                            Toast.makeText(activity, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //查询生活建议，若失败则提示，若成功则保存到本地并加载界面
    public void requestWeatherSuggestions(final String weatherId) {
        String weatherUrl =
                "https://free-api.heweather.net/s6/weather/lifestyle?location=" + weatherId +
                        "&key=1872edf195e5429f88b0c48e723f36b0";
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                Utility.handleWeatherSuggestions(responseText, weather);
                SharedPreferences.Editor editor = activity.getSharedPreferences(
                        "weather", Context.MODE_MULTI_PROCESS).edit();
                editor.putString("WEATHER_SUGGESTIONS", responseText);
                editor.apply();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather.suggestion != null) {
                            showWeatherSuggestions(weather.suggestion);
                        } else {
                            Toast.makeText(activity, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //以下三个函数仅作界面的加载
    private void showWeatherNow(Weather weatherInfo) {
        String cityName = weatherInfo.basic.cityName;
        String updateTime = weatherInfo.update.updateTime.split(" ")[1];
        String info = weatherInfo.now.conditionDay;
        String degree = weatherInfo.now.temperature + "℃";
        String bodyFeelingTemperature = "体感温度：" + weatherInfo.now.bodyFeelingTemperature + "℃";
        String atmosphericPressure = "大气压：" + weatherInfo.now.atmosphericPressure + "hPa";
        String humidity = "湿度：" + weatherInfo.now.humidity + "%";
        String visibility = "能见度：" + weatherInfo.now.visibility + "km";
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        weatherInfoText.setText(info);
        degreeText.setText(degree);
        bodyFeelingTemperatureText.setText(bodyFeelingTemperature);
        pressureText.setText(atmosphericPressure);
        humidityText.setText(humidity);
        visibilityText.setText(visibility);
    }

    private void showWeatherForecast(List<Forecast> forecastList) {
        forecastLayout.removeAllViews();
        for (Forecast forecast : forecastList) {
            View view =
                    LayoutInflater.from(getContext()).inflate(R.layout.weather_frag_forecast_item
                            , forecastLayout, false);
            TextView dateText = view.findViewById(R.id.forecast_item_date_text);
            TextView infoText = view.findViewById(R.id.forecast_item_info_text);
            TextView minText = view.findViewById(R.id.forecast_item_min_text);
            TextView maxText = view.findViewById(R.id.forecast_item_max_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.conditionDay);
            minText.setText(forecast.tempMin + "℃");
            maxText.setText(forecast.tempMax + "℃");
            forecastLayout.addView(view);
        }
    }

    private void showWeatherSuggestions(Suggestion suggestion) {
        String comfort = "舒适度：" + suggestion.comfort;
        String carWash = "洗车指数：" + suggestion.carWash;
        String sport = "运动建议：" + suggestion.sport;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
    }
}
