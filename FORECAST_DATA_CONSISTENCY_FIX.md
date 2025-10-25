# Forecast Data Consistency Fix

## Issue Identified

The 3-day forecast (shown on Dashboard) and 7-day forecast (shown in ForecastActivity) were displaying **contradicting information** for the same dates.

## Root Causes

### 1. Different API Request Parameters
- **DashboardActivity**: Was requesting only **3 days** from the Weatherbit API
- **ForecastActivity**: Was requesting **16 days** from the Weatherbit API

This caused issues because:
- The API calls were made at different times (seconds or minutes apart)
- Weather data can be updated by Weatherbit between calls
- Different request parameters may result in different cached responses
- No data sharing mechanism existed between the two screens

### 2. Inconsistent SharedPreferences Usage
- **DashboardActivity**: 
  - Used `PREFS_NAME = "LocationPrefs"` (with capital 'L')
  - Always used hardcoded `WEATHERBIT_API_KEY` constant
  
- **ForecastActivity**: 
  - Used `PREFS_NAME = "location_prefs"` (lowercase)
  - Retrieved API key from SharedPreferences with hardcoded value as fallback
  - Saved API key to SharedPreferences on initialization

This inconsistency meant:
- User preferences could be stored in two different SharedPreferences files
- Custom API keys saved in settings would only work in ForecastActivity, not DashboardActivity
- Potential data synchronization issues between screens

## Solutions Implemented

### 1. Unified API Request Parameters
**Changed in `DashboardActivity.java` (line 282):**
```java
// Before: Requested only 3 days
weatherApiService.getForecast7Day(lat, lng, 3, "M", WEATHERBIT_API_KEY)

// After: Request 16 days (same as ForecastActivity), display first 3
weatherApiService.getForecast7Day(lat, lng, 16, "M", apiKey)
```

**Changed in `DashboardActivity.java` (lines 288-296):**
```java
// Now extracts first 3 days from the full response
List<ForecastDay> allDays = forecastResponse.getForecastDays();
android.util.Log.d("DashboardActivity", "Received " + allDays.size() + " forecast days from API");

// Take only the first 3 days for dashboard display
forecastData.clear();
int daysToShow = Math.min(3, allDays.size());
forecastData.addAll(allDays.subList(0, daysToShow));
forecastAdapter.notifyDataSetChanged();
android.util.Log.d("DashboardActivity", "Displaying first " + daysToShow + " forecast days on dashboard");
```

### 2. Unified SharedPreferences Configuration
**Changed in `DashboardActivity.java` (lines 44-49):**
```java
// Before:
private static final String PREFS_NAME = "LocationPrefs";
// (No KEY_WEATHERBIT_API_KEY constant)
// (Always used hardcoded WEATHERBIT_API_KEY)

// After:
private static final String PREFS_NAME = "location_prefs"; // Changed to match ForecastActivity
private static final String KEY_WEATHERBIT_API_KEY = "weatherbit_api_key"; // Added for consistency
```

**Changed in `DashboardActivity.java` (lines 219-225):**
```java
// Before: Always used hardcoded API key
weatherApiService.getCurrentWeather(lat, lng, WEATHERBIT_API_KEY, "M")

// After: Retrieves from SharedPreferences with fallback (same as ForecastActivity)
String apiKey = sharedPreferences.getString(KEY_WEATHERBIT_API_KEY, WEATHERBIT_API_KEY);
weatherApiService.getCurrentWeather(lat, lng, apiKey, "M")
```

## Benefits of This Fix

1. **Data Consistency**: Both screens now request the same dataset (16 days) from the API
2. **Cache Efficiency**: Weatherbit API can serve cached responses for identical requests
3. **Reduced API Calls**: Potential for better caching reduces API usage
4. **Unified Configuration**: Both activities now use the same SharedPreferences store
5. **Custom API Key Support**: DashboardActivity now respects custom API keys saved in settings
6. **Better Debugging**: Both activities log the same amount of data for troubleshooting

## Testing Recommendations

1. **Clear App Data**: Clear the app's data/cache to reset SharedPreferences
2. **Check Consistency**: Compare the first 3 days shown on Dashboard with the first 3 days in ForecastActivity
3. **Verify API Usage**: Monitor API call logs to ensure both activities use the same parameters
4. **Custom API Keys**: Test that custom API keys set in settings work for both screens
5. **Network Conditions**: Test under various network conditions to ensure data remains consistent

## Technical Notes

- Both activities now request **16 days** (the maximum free tier allows on most Weatherbit plans)
- DashboardActivity displays the **first 3 days** from the response
- ForecastActivity displays the **first 7 days** from the response
- The Weatherbit API may cache identical requests, improving response times and reducing quota usage
- SharedPreferences key `"weatherbit_api_key"` is now used consistently across both activities
- SharedPreferences file `"location_prefs"` is now used consistently across both activities

## Additional Fix: Location Selection Navigation Issue

### Problem
After the initial SharedPreferences fix, a new issue emerged: selecting a location wouldn't navigate to the dashboard.

**Root Cause**: LocationActivity was still using the old SharedPreferences name `"LocationPrefs"` while DashboardActivity was updated to use `"location_prefs"`. This created a mismatch where:
1. LocationActivity saved location → `"LocationPrefs"` file
2. DashboardActivity looked for location → `"location_prefs"` file  
3. DashboardActivity couldn't find location → redirected back to LocationActivity
4. Infinite redirect loop!

### Solution Applied
**Changed in `LocationActivity.java` (line 40):**
```java
// Before:
private static final String PREFS_NAME = "LocationPrefs";

// After:
private static final String PREFS_NAME = "location_prefs"; // Changed to match DashboardActivity and ForecastActivity
```

Now all three activities use the same SharedPreferences file: `"location_prefs"`

## Related Files Modified

- `app/src/main/java/com/mzansi/solutions/disasterdetectoralert/DashboardActivity.java`
- `app/src/main/java/com/mzansi/solutions/disasterdetectoralert/LocationActivity.java`

## Date Fixed

October 21, 2025

