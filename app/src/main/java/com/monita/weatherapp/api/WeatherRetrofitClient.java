package com.monita.weatherapp.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherRetrofitClient {
    public static String API_KEY="f705bd32f2206485c32dd937903a2d13";
    private String BASE_URL="https://api.openweathermap.org";
    private WeatherApi weatherApi;
    private static WeatherRetrofitClient weatherRetrofitClient=null;

    private WeatherRetrofitClient()
    {
        Gson gson = new GsonBuilder().setLenient().create();
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson)).build();
//        Retrofit retrofit=new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
//                .build();
        weatherApi=retrofit.create(WeatherApi.class);
    }

    public static synchronized  WeatherRetrofitClient getInstance()
    {
        if(weatherRetrofitClient ==null)
        {
            weatherRetrofitClient= new WeatherRetrofitClient();
        }
        return weatherRetrofitClient;
    }

    public WeatherApi getWeatherApi()
    {
        return weatherApi;
    }
}
