package com.mzansi.solutions.disasterdetectoralert.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {
    @SerializedName("data")
    private List<ForecastDay> forecastDays;
    
    @SerializedName("city_name")
    private String cityName;
    
    @SerializedName("country_code")
    private String countryCode;
    
    @SerializedName("timezone")
    private String timezone;
    
    // Getters
    public List<ForecastDay> getForecastDays() { return forecastDays; }
    public String getCityName() { return cityName; }
    public String getCountryCode() { return countryCode; }
    public String getTimezone() { return timezone; }
}



