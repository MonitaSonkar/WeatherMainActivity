package com.monita.weatherapp.api;

import com.monita.weatherapp.model.CityWeather;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("/data/2.5/weather")
    Call<CityWeather> getCityWeather(@Query("lat")double city, @Query("lon")double limit, @Query("appid")String appid);

}
