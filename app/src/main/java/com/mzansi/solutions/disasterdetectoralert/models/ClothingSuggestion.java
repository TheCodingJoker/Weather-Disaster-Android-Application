package com.mzansi.solutions.disasterdetectoralert.models;

public class ClothingSuggestion {
    private String day;
    private String weatherCondition;
    private String temperature;
    private String suggestion;
    private long timestamp;
    
    public ClothingSuggestion() {}
    
    public ClothingSuggestion(String day, String weatherCondition, String temperature, String suggestion) {
        this.day = day;
        this.weatherCondition = weatherCondition;
        this.temperature = temperature;
        this.suggestion = suggestion;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    
    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }
    
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}




