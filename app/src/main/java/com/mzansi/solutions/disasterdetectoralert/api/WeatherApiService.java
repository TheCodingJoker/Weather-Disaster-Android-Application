package com.mzansi.solutions.disasterdetectoralert.api;

import com.mzansi.solutions.disasterdetectoralert.models.ForecastResponse;
import com.mzansi.solutions.disasterdetectoralert.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    
    @GET("current")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("key") String apiKey,
            @Query("units") String units
    );
    
    @GET("forecast/daily")
    Call<ForecastResponse> getForecast(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("key") String apiKey,
            @Query("days") int days,
            @Query("units") String units
    );
    
    @GET("forecast/daily")
    Call<ForecastResponse> getForecast7Day(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("days") int days,
            @Query("units") String units,
            @Query("key") String apiKey
    );
}
