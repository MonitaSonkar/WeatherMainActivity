package com.monita.weatherapp.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.monita.weatherapp.MainActivity;
import com.monita.weatherapp.api.WeatherRetrofitClient;
import com.monita.weatherapp.model.CityWeather;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private MutableLiveData<CityWeather> weatherMutableLiveData= new MutableLiveData<>();
    public void getWeatherInfo(double latitude, double longitude)
    {
        Call<CityWeather> getLatLong = WeatherRetrofitClient.getInstance().getWeatherApi().getCityWeather(latitude, longitude, WeatherRetrofitClient.API_KEY);
        getLatLong.enqueue(new Callback<CityWeather>() {
            @Override
            public void onResponse(Call<CityWeather> call, Response<CityWeather> response) {
                weatherMutableLiveData.postValue(response.body());
            }

            @Override
            public void onFailure(Call<CityWeather> call, Throwable t) {
                weatherMutableLiveData.postValue(null);
            }
        });
    }


    public LiveData<CityWeather> getWeatherLiveData() {
        return weatherMutableLiveData;
    }
    public void getCityLatLong(Geocoder gcd, String City, Context mainActivity) {
        try {
            List<Address> addresses = gcd.getFromLocationName(City, 2);
            for (Address addr : addresses) {
                if (addr != null) {
                    double longitude = addr.getLongitude();
                    double latitude = addr.getLatitude();
                    saveCity(City,mainActivity);
                    getWeatherInfo(latitude,longitude);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            weatherMutableLiveData.postValue(null);
        }

    }
    public static void saveCity(String city, Context mainActivity) {
        SharedPreferences.Editor prefs = mainActivity.getSharedPreferences("WeatherApp",
                Context.MODE_PRIVATE).edit();
        prefs.putString("CityName", city);
        prefs.commit();
    }

    public static String getCity(MainActivity mainActivity) {
        final SharedPreferences prefs = mainActivity.getSharedPreferences(
                "WeatherApp", Context.MODE_PRIVATE);
        return (prefs.getString("CityName", null));
    }

}
