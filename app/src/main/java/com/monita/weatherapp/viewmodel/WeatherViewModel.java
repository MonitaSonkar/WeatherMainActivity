package com.monita.weatherapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.location.Geocoder;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.monita.weatherapp.MainActivity;
import com.monita.weatherapp.model.CityWeather;
import com.monita.weatherapp.model.Weather;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

public class WeatherViewModel extends AndroidViewModel {
    private WeatherRepository weatherRepository;
    private LiveData<CityWeather> cityWeatherLiveData;

    public WeatherViewModel(@NonNull Application application) {
        super(application);
    }


    public void init() {
        weatherRepository = new WeatherRepository();
        cityWeatherLiveData = weatherRepository.getWeatherLiveData();
    }

    public void searchCityWeather(Geocoder gcd, String city, Context mainActivity) {
        weatherRepository.getCityLatLong(gcd,city,mainActivity);
    }

    public void searchCityWeather(double lat, double lon) {
        weatherRepository.getWeatherInfo(lat,lon);
    }


    public LiveData<CityWeather> getCityWeatherLiveData() {
        return cityWeatherLiveData;
    }
}
