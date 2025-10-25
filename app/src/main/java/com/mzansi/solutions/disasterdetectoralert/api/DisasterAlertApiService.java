package com.mzansi.solutions.disasterdetectoralert.api;

import com.mzansi.solutions.disasterdetectoralert.models.DisasterAlertResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DisasterAlertApiService {
    
    @GET("alerts")
    Call<DisasterAlertResponse> getDisasterAlerts(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("radius") int radiusKm,
            @Query("api_key") String apiKey
    );
    
    @GET("alerts/active")
    Call<DisasterAlertResponse> getActiveDisasterAlerts(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("radius") int radiusKm,
            @Query("api_key") String apiKey
    );
}




