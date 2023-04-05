package com.monita.weatherapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.monita.weatherapp.api.WeatherRetrofitClient;
import com.monita.weatherapp.model.CityWeather;
import com.monita.weatherapp.viewmodel.WeatherRepository;
import com.monita.weatherapp.viewmodel.WeatherViewModel;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView cityNameTV, temperature, condition;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconTV, searchIV;
    private int PERMISSION_CODE = 1;
    private ProgressDialog dialog;
    private String imgpath = "";
    private RelativeLayout rlHome;
    DecimalFormat df = new DecimalFormat("#.#");
    private WeatherViewModel weatherViewModel;
    private Geocoder gcd;
    private ProgressBar progressBar;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);
        weatherViewModel.init();
        setContentView(R.layout.activity_main);

        gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        dialog = new ProgressDialog(MainActivity.this);
        rlHome = findViewById(R.id.rl_home);
        progressBar = findViewById(R.id.loader_pb);
        cityNameTV = findViewById(R.id.tv_cityname);
        temperature = findViewById(R.id.temperature);
        condition = findViewById(R.id.condition);
        cityEdt = findViewById(R.id.edt_city);
        backIV = findViewById(R.id.background_img);
        iconTV = findViewById(R.id.climate_imge);
        searchIV = findViewById(R.id.search_img);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_CODE);
        }
        if (!TextUtils.isEmpty(WeatherRepository.getCity(MainActivity.this))) {
            String city = WeatherRepository.getCity(MainActivity.this);
            cityEdt.setText(city);
            progressBar.setVisibility(View.GONE);
            rlHome.setVisibility(View.VISIBLE);
            dialog.setMessage("Loading...");
            dialog.show();
            weatherViewModel.searchCityWeather(gcd, city, MainActivity.this);
        }

        weatherViewModel.getCityWeatherLiveData().observe(this, new Observer<CityWeather>() {
            @Override
            public void onChanged(CityWeather cityWeather) {
                dialog.dismiss();
                if (cityWeather != null) {
                    String description = cityWeather.getWeather().get(0).getMain();
                    imgpath = "https://openweathermap.org/img/w/" + cityWeather.getWeather().get(0).getIcon() + ".png";
                    double temp = cityWeather.getMain().getTemp() - 273.15;
                    temperature.setText(df.format(temp) + " °C");
                    condition.setText(description);
                    progressBar.setVisibility(View.GONE);
                    rlHome.setVisibility(View.VISIBLE);
                    Glide.with(MainActivity.this)
                            .load(imgpath)
                            .override(300, 300)
                            .fitCenter()
                            .into(iconTV);
                }
            }
        });

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter the city name", Toast.LENGTH_LONG).show();
                } else {
                    dialog.setMessage("Loading...");
                    dialog.show();
                    cityNameTV.setText(city);
                    weatherViewModel.searchCityWeather(gcd, city, MainActivity.this);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    getCurrentLocation();

                } else {

                    turnOnGPS();
                }

            } else {
                Toast.makeText(this, "Please provide location the permission", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() > 0) {

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();
                                        weatherViewModel.searchCityWeather(latitude, longitude);
                                        getCityfromLatLong(latitude, longitude);
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }

    private void getCityfromLatLong(double lati, double longi) {
        String cityName = "Not Found";
        try {
            List<Address> addresses = gcd.getFromLocation(lati, longi, 10);
            for (Address addr : addresses) {
                if (addr != null) {
                    String city = addr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                        cityNameTV.setText(cityName);
                        cityEdt.setText(cityName);
                        WeatherRepository.saveCity(cityName, MainActivity.this);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}