package com.mzansi.solutions.disasterdetectoralert.models;

import com.google.gson.annotations.SerializedName;

public class WeatherData {
    @SerializedName("temp")
    private double temperature;
    
    @SerializedName("app_temp")
    private double feelsLike;
    
    @SerializedName("rh")
    private double humidity;
    
    @SerializedName("wind_spd")
    private double windSpeed;
    
    @SerializedName("wind_dir")
    private double windDirection;
    
    @SerializedName("pres")
    private double pressure;
    
    @SerializedName("vis")
    private double visibility;
    
    @SerializedName("uv")
    private double uvIndex;
    
    @SerializedName("weather")
    private Weather weather;
    
    @SerializedName("datetime")
    private String datetime;
    
    @SerializedName("city_name")
    private String cityName;
    
    @SerializedName("country_code")
    private String countryCode;

    // Getters and Setters
    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(double windDirection) {
        this.windDirection = windDirection;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getVisibility() {
        return visibility;
    }

    public void setVisibility(double visibility) {
        this.visibility = visibility;
    }

    public double getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}



