# Forecast Loading Troubleshooting Guide

## 🔍 **Common Issues and Solutions**

### **1. API Key Issues**
**Problem**: Invalid or expired API key
**Solution**: 
- Verify the API key is correct in `local.properties`
- Check if the key has expired or reached usage limits
- Ensure the key has forecast permissions

### **2. Network Connectivity**
**Problem**: No internet connection or poor connectivity
**Solution**:
- Check device internet connection
- Try switching between WiFi and mobile data
- Verify network permissions in AndroidManifest.xml

### **3. API Endpoint Issues**
**Problem**: Incorrect API endpoint or parameters
**Solution**:
- Updated API service to use correct parameter format
- Changed from string concatenation to separate lat/lon parameters
- Fixed units parameter from "metric" to "M"

### **4. Location Data Issues**
**Problem**: Invalid or missing location data
**Solution**:
- Check SharedPreferences for saved location
- Default fallback to Cape Town coordinates (-33.9249, 18.4241)
- Verify location permissions are granted

## 🛠️ **Debug Features Added**

### **Enhanced Logging**
```java
// Location and API key logging
android.util.Log.d("ForecastActivity", "Using location: " + lat + ", " + lng);
android.util.Log.d("ForecastActivity", "API Key: " + WEATHERBIT_API_KEY.substring(0, 8) + "...");

// API response error logging
android.util.Log.e("ForecastActivity", "API Error: " + response.code() + " - " + response.message());
android.util.Log.e("ForecastActivity", "Error body: " + response.errorBody().string());

// Network error logging
android.util.Log.e("ForecastActivity", "Network error: " + t.getMessage());
```

### **Fallback Data System**
- Automatically loads mock data if API fails
- Shows user-friendly message about offline data
- Allows testing of UI components without API dependency

## 📱 **Testing Steps**

### **1. Check Logs**
Run the app and check Android Studio Logcat for:
- Location coordinates being used
- API key format
- HTTP response codes
- Error messages

### **2. Test API Directly**
Use a tool like Postman to test the API:
```
GET https://api.weatherbit.io/v2.0/forecast/daily
Parameters:
- lat: -33.9249
- lon: 18.4241
- days: 7
- units: M
- key: YOUR_WEATHERBIT_API_KEY_HERE
```

### **3. Verify Permissions**
Check AndroidManifest.xml has:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🔧 **API Service Fixes Applied**

### **Before (Incorrect)**:
```java
@GET("forecast/daily")
Call<ForecastResponse> getForecast7Day(
    @Query("lat") String latLon,  // Wrong: concatenated string
    @Query("days") String days,   // Wrong: string instead of int
    @Query("units") String units,
    @Query("key") String apiKey
);
```

### **After (Correct)**:
```java
@GET("forecast/daily")
Call<ForecastResponse> getForecast7Day(
    @Query("lat") double latitude,    // Correct: separate lat parameter
    @Query("lon") double longitude,   // Correct: separate lon parameter
    @Query("days") int days,          // Correct: integer type
    @Query("units") String units,
    @Query("key") String apiKey
);
```

## 🚀 **Expected Behavior**

### **Success Case**:
1. App loads forecast data from Weatherbit API
2. 7-day forecast cards display with weather information
3. Each day shows temperature, weather description, precipitation chance
4. Generate clothing suggestion buttons work

### **Fallback Case**:
1. If API fails, fallback data loads automatically
2. Toast message shows "Using offline data - check internet connection"
3. UI still functions for testing purposes

## 📊 **API Response Format**

Expected Weatherbit API response:
```json
{
  "data": [
    {
      "datetime": "2024-12-25",
      "max_temp": 28.5,
      "min_temp": 18.2,
      "weather": {
        "description": "Partly Cloudy",
        "icon": "c02d"
      },
      "precip": 0.5,
      "wind_spd": 15.2,
      "rh": 65.0
    }
  ],
  "city_name": "Cape Town",
  "country_code": "ZA"
}
```

## 🔍 **Next Steps**

1. **Run the app** and check Logcat for error messages
2. **Test API connectivity** using the debug logs
3. **Verify location data** is being saved correctly
4. **Check network permissions** are granted
5. **Test fallback data** if API continues to fail

The enhanced logging and fallback system should help identify the exact cause of the forecast loading issue!



