package com.mzansi.solutions.disasterdetectoralert.models;

import com.google.gson.annotations.SerializedName;

public class ForecastDay {
    @SerializedName("datetime")
    private String datetime;
    
    @SerializedName("temp")
    private double temp;
    
    @SerializedName("max_temp")
    private double maxTemp;
    
    @SerializedName("min_temp")
    private double minTemp;
    
    @SerializedName("weather")
    private Weather weather;
    
    @SerializedName("precip")
    private double precipitation;
    
    @SerializedName("wind_spd")
    private double windSpeed;
    
    @SerializedName("wind_dir")
    private double windDirection;
    
    @SerializedName("rh")
    private double humidity;
    
    @SerializedName("uv")
    private double uvIndex;
    
    @SerializedName("pop")
    private double precipitationProbability;
    
    @SerializedName("snow")
    private double snow;
    
    @SerializedName("sunrise_ts")
    private long sunriseTimestamp;
    
    @SerializedName("sunset_ts")
    private long sunsetTimestamp;
    
    // Getters
    public String getDatetime() { return datetime; }
    public double getTemp() { return temp; }
    public double getMaxTemp() { return maxTemp; }
    public double getMinTemp() { return minTemp; }
    public Weather getWeather() { return weather; }
    public double getPrecipitation() { return precipitation; }
    public double getWindSpeed() { return windSpeed; }
    public double getWindDirection() { return windDirection; }
    public double getHumidity() { return humidity; }
    public double getUvIndex() { return uvIndex; }
    public double getPrecipitationProbability() { return precipitationProbability; }
    public double getSnow() { return snow; }
    public long getSunriseTimestamp() { return sunriseTimestamp; }
    public long getSunsetTimestamp() { return sunsetTimestamp; }
    
    // Setters
    public void setDatetime(String datetime) { this.datetime = datetime; }
    public void setTemp(double temp) { this.temp = temp; }
    public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }
    public void setMinTemp(double minTemp) { this.minTemp = minTemp; }
    public void setWeather(Weather weather) { this.weather = weather; }
    public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
    public void setWindDirection(double windDirection) { this.windDirection = windDirection; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    public void setUvIndex(double uvIndex) { this.uvIndex = uvIndex; }
    public void setPrecipitationProbability(double precipitationProbability) { this.precipitationProbability = precipitationProbability; }
    public void setSnow(double snow) { this.snow = snow; }
    public void setSunriseTimestamp(long sunriseTimestamp) { this.sunriseTimestamp = sunriseTimestamp; }
    public void setSunsetTimestamp(long sunsetTimestamp) { this.sunsetTimestamp = sunsetTimestamp; }
    
    // Helper methods
    public String getFormattedDate() {
        if (datetime != null && datetime.length() >= 10) {
            return datetime.substring(5, 10); // MM-DD format
        }
        return datetime;
    }
    
    public String getDayOfWeek() {
        if (datetime != null && datetime.length() >= 10) {
            try {
                String[] parts = datetime.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.set(year, month - 1, day);
                
                String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                return days[calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1];
            } catch (Exception e) {
                return "N/A";
            }
        }
        return "N/A";
    }
}
