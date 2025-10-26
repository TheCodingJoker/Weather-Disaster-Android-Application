package com.mzansi.solutions.disasterdetectoralert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.mzansi.solutions.disasterdetectoralert.adapters.WeatherForecastAdapter;
import com.mzansi.solutions.disasterdetectoralert.api.ApiClient;
import com.mzansi.solutions.disasterdetectoralert.api.GeminiApiService;
import com.mzansi.solutions.disasterdetectoralert.api.WeatherApiService;
import com.mzansi.solutions.disasterdetectoralert.models.Crop;
import com.mzansi.solutions.disasterdetectoralert.models.DisasterAlert;
import com.mzansi.solutions.disasterdetectoralert.models.ForecastDay;
import com.mzansi.solutions.disasterdetectoralert.models.ForecastResponse;
import com.mzansi.solutions.disasterdetectoralert.models.WeatherData;
import com.mzansi.solutions.disasterdetectoralert.models.WeatherResponse;
import com.mzansi.solutions.disasterdetectoralert.utils.CropManager;
import com.mzansi.solutions.disasterdetectoralert.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FarmerDashboardActivity extends AppCompatActivity {

    private static final String TAG = "FarmerDashboard";
    private static final String PREFS_NAME = "farmer_location_prefs";
    private static final String KEY_LOCATION_NAME = "location_name";
    private static final String KEY_LOCATION_LAT = "location_lat";
    private static final String KEY_LOCATION_LNG = "location_lng";
    private static final String API_PREFS = "api_prefs";
    private static final String KEY_WEATHER_API = "weatherbit_api_key";

    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvCurrentTemp;
    private TextView tvWeatherDescription;
    private TextView tvLocationName;
    private ImageView ivWeatherIcon;
    private TextView tvHumidity;
    private TextView tvWindSpeed;
    private TextView tvPrecipitation;
    private RecyclerView rvForecast;
    private LineChart chartClimate;
    private RecyclerView rvDisasterAlerts;
    private TextView tvNoAlerts;
    private TextView tvAlertCount;
    private FrameLayout loadingOverlay;
    private BottomNavigationView bottomNavigation;
    
    // New AI & Crop Components
    private TextView tvUpcomingTasks;

    // Data
    private SharedPreferences locationPrefs;
    private SharedPreferences apiPrefs;
    private WeatherForecastAdapter forecastAdapter;
    private com.mzansi.solutions.disasterdetectoralert.adapters.DisasterAlertAdapter alertAdapter;
    private CropManager cropManager;
    private GeminiApiService geminiApiService;
    private String apiKey;
    private double latitude;
    private double longitude;
    private String locationName;
    private List<ForecastDay> weatherForecastData;
    private List<DisasterAlert> disasterAlerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is logged in
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            // User not logged in, redirect to login
            Intent intent = new Intent(this, FarmerLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_farmer_dashboard);

        initializeViews();
        initializeData();
        setupRecyclerView();
        setupBottomNavigation();
        setupClickListeners();
        loadWeatherData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvWeatherDescription = findViewById(R.id.tvWeatherDescription);
        tvLocationName = findViewById(R.id.tvLocationName);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        tvPrecipitation = findViewById(R.id.tvPrecipitation);
        rvForecast = findViewById(R.id.rvForecast);
        chartClimate = findViewById(R.id.chartClimate);
        rvDisasterAlerts = findViewById(R.id.rvDisasterAlerts);
        tvNoAlerts = findViewById(R.id.tvNoAlerts);
        tvAlertCount = findViewById(R.id.tvAlertCount);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // New advanced features views
        tvUpcomingTasks = findViewById(R.id.tvUpcomingTasks);
    }

    private void initializeData() {
        locationPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        apiPrefs = getSharedPreferences(API_PREFS, MODE_PRIVATE);
        
        // Initialize crop manager and Gemini AI service
        cropManager = new CropManager(this);
        geminiApiService = new GeminiApiService();
        
        // Get API key
        apiKey = apiPrefs.getString(KEY_WEATHER_API, BuildConfig.WEATHERBIT_API_KEY);
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Weather API key not configured", Toast.LENGTH_LONG).show();
            Log.e(TAG, "API key is missing");
        }

        // Get location
        locationName = locationPrefs.getString(KEY_LOCATION_NAME, "Unknown Location");
        latitude = locationPrefs.getFloat(KEY_LOCATION_LAT, 0f);
        longitude = locationPrefs.getFloat(KEY_LOCATION_LNG, 0f);
        
        // Set location name in UI
        tvLocationName.setText(locationName);
        
        Log.d(TAG, "Location: " + locationName + " (" + latitude + ", " + longitude + ")");
    }

    private void setupRecyclerView() {
        // Weather forecast
        forecastAdapter = new WeatherForecastAdapter(new ArrayList<>());
        rvForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvForecast.setAdapter(forecastAdapter);
        
        // Disaster alerts
        disasterAlerts = new ArrayList<>();
        alertAdapter = new com.mzansi.solutions.disasterdetectoralert.adapters.DisasterAlertAdapter(disasterAlerts);
        rvDisasterAlerts.setLayoutManager(new LinearLayoutManager(this));
        rvDisasterAlerts.setAdapter(alertAdapter);
        
        // Crops are now managed in CropManagementActivity
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                return true;
            } else if (itemId == R.id.nav_crops) {
                // Navigate to Crop Management Activity
                startActivity(new Intent(this, CropManagementActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_forecast) {
                startActivity(new Intent(this, FarmerForecastActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, FarmerSettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        // Toolbar back button
        toolbar.setNavigationOnClickListener(v -> showExitDialog());
    }
    
    @Override
    public void onBackPressed() {
        showExitDialog();
    }
    
    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do you really want to leave the app?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear session and logout
                    SessionManager sessionManager = new SessionManager(this);
                    sessionManager.logout();
                    
                    // Clear farmer location preferences
                    SharedPreferences farmerPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    farmerPrefs.edit().clear().apply();
                    
                    // Exit the app completely
                    finishAffinity();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss dialog
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Crops are managed in CropManagementActivity
        if (weatherForecastData != null) {
            generateUpcomingTasks(weatherForecastData);
        }
    }

    private void loadWeatherData() {
        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "Location not set. Please select a location.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LocationActivity.class);
            intent.putExtra("is_farmer", true); // Farmer
            startActivity(intent);
            finish();
            return;
        }

        showLoading(true);
        
        WeatherApiService apiService = ApiClient.getWeatherService();
        
        // Fetch current weather for today's display
        fetchCurrentWeather(apiService, latitude, longitude, apiKey);
        
        // Fetch forecast data for 3-day forecast, chart, and analysis
        fetchForecastData(apiService, latitude, longitude, apiKey);
    }
    
    private void fetchCurrentWeather(WeatherApiService apiService, double latitude, double longitude, String apiKey) {
        Log.d(TAG, "Fetching current weather for: " + latitude + ", " + longitude);
        Call<WeatherResponse> currentCall = apiService.getCurrentWeather(latitude, longitude, apiKey, "M");
        
        currentCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getData().isEmpty()) {
                    WeatherData currentWeather = response.body().getData().get(0);
                    Log.d(TAG, "✅ Current weather loaded: " + currentWeather.getTemperature() + "°C");
                    displayTodayWeatherFromCurrent(currentWeather);
                } else {
                    Log.e(TAG, "========================================");
                    Log.e(TAG, "CURRENT WEATHER API ERROR:");
                    Log.e(TAG, "Response Code: " + response.code());
                    Log.e(TAG, "Response Message: " + response.message());
                    
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Error Body: " + errorBody);
                    Log.e(TAG, "Request URL: " + call.request().url());
                    Log.e(TAG, "========================================");
                }
            }
            
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "========================================");
                Log.e(TAG, "CURRENT WEATHER NETWORK ERROR:");
                Log.e(TAG, "Error: " + t.getMessage());
                Log.e(TAG, "========================================");
                t.printStackTrace();
            }
        });
    }
    
    private void fetchForecastData(WeatherApiService apiService, double latitude, double longitude, String apiKey) {
        Call<ForecastResponse> forecastCall = apiService.getForecast(
                latitude,
                longitude,
                apiKey,
                16, // Request 16 days to get historical-like data
                "M" // Metric units
        );

        forecastCall.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse weatherData = response.body();
                    
                    if (weatherData.getForecastDays() != null && !weatherData.getForecastDays().isEmpty()) {
                        Log.d(TAG, "Forecast data loaded successfully: " + weatherData.getForecastDays().size() + " days");
                        
                        // Update precipitation from today's forecast (not available in /current endpoint)
                        ForecastDay today = weatherData.getForecastDays().get(0);
                        tvPrecipitation.setText(String.format(Locale.getDefault(), "%.1f mm", today.getPrecipitation()));
                        
                        // Display 3-day forecast (items 1-3, skipping today which is index 0)
                        display3DayForecast(weatherData.getForecastDays());
                        
                        // Display historical climate graph (first 7 days)
                        displayClimateChart(weatherData.getForecastDays());
                        
                        // Analyze and display disaster alerts
                        analyzeDisasterRisks(weatherData.getForecastDays());
                        
                        // Generate upcoming tasks
                        weatherForecastData = weatherData.getForecastDays();
                        generateUpcomingTasks(weatherForecastData);
                        
                    } else {
                        Log.e(TAG, "Weather response body is empty or null");
                        Toast.makeText(FarmerDashboardActivity.this, "No weather data available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Enhanced error logging
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    
                    Log.e(TAG, "========================================");
                    Log.e(TAG, "FORECAST API ERROR:");
                    Log.e(TAG, "Response Code: " + response.code());
                    Log.e(TAG, "Response Message: " + response.message());
                    Log.e(TAG, "Error Body: " + errorBody);
                    Log.e(TAG, "API Key (first 20 chars): " + (apiKey.length() > 20 ? apiKey.substring(0, 20) + "..." : apiKey));
                    Log.e(TAG, "Location: " + latitude + ", " + longitude);
                    Log.e(TAG, "Request URL: " + call.request().url());
                    Log.e(TAG, "========================================");
                    
                    String errorMessage = "Failed to load weather data";
                    if (response.code() == 401 || response.code() == 403) {
                        errorMessage = "Invalid API Key. Check settings.";
                    } else if (response.code() == 429) {
                        errorMessage = "API rate limit exceeded. Try again later.";
                    } else if (response.code() == 500) {
                        errorMessage = "Weather service error. Try again later.";
                    }
                    
                    Toast.makeText(FarmerDashboardActivity.this, errorMessage + " (Error " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "========================================");
                Log.e(TAG, "FORECAST NETWORK ERROR:");
                Log.e(TAG, "Error: " + t.getClass().getSimpleName());
                Log.e(TAG, "Message: " + t.getMessage());
                Log.e(TAG, "Cause: " + (t.getCause() != null ? t.getCause().getMessage() : "None"));
                Log.e(TAG, "========================================");
                t.printStackTrace();
                
                Toast.makeText(FarmerDashboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayTodayWeatherFromCurrent(WeatherData currentWeather) {
        // Temperature (real-time current weather)
        tvCurrentTemp.setText(String.format(Locale.getDefault(), "%.0f°C", currentWeather.getTemperature()));
        
        // Description
        if (currentWeather.getWeather() != null) {
            tvWeatherDescription.setText(currentWeather.getWeather().getDescription());
            
            // Weather icon
            String iconUrl = currentWeather.getWeather().getIconUrl();
            Glide.with(this)
                    .load(iconUrl)
                    .into(ivWeatherIcon);
        }
        
        // Humidity
        tvHumidity.setText(String.format(Locale.getDefault(), "%.0f%%", currentWeather.getHumidity()));
        
        // Wind speed
        tvWindSpeed.setText(String.format(Locale.getDefault(), "%.1f m/s", currentWeather.getWindSpeed()));
        
        // Note: Precipitation data is not available from /current endpoint
        // It will be updated when forecast data loads
    }

    private void displayTodayWeather(ForecastDay today) {
        // Temperature
        tvCurrentTemp.setText(String.format(Locale.getDefault(), "%.0f°C", today.getTemp()));
        
        // Description
        if (today.getWeather() != null) {
            tvWeatherDescription.setText(today.getWeather().getDescription());
            
            // Weather icon
            String iconCode = today.getWeather().getIcon();
            String iconUrl = "https://www.weatherbit.io/static/img/icons/" + iconCode + ".png";
            Glide.with(this)
                    .load(iconUrl)
                    .into(ivWeatherIcon);
        }
        
        // Humidity
        tvHumidity.setText(String.format(Locale.getDefault(), "%.0f%%", today.getHumidity()));
        
        // Wind speed
        tvWindSpeed.setText(String.format(Locale.getDefault(), "%.1f m/s", today.getWindSpeed()));
        
        // Precipitation
        tvPrecipitation.setText(String.format(Locale.getDefault(), "%.1f mm", today.getPrecipitation()));
    }

    private void display3DayForecast(List<ForecastDay> allDays) {
        // Get exactly 3 days (skip today which is day 0)
        List<ForecastDay> threeDayForecast = new ArrayList<>();
        
        // Add days 1, 2, and 3 (indices 1, 2, 3) to show exactly 3 days
        for (int i = 1; i <= 3 && i < allDays.size(); i++) {
            ForecastDay day = allDays.get(i);
            threeDayForecast.add(day);
            Log.d(TAG, "Added day " + i + " to 3-day forecast: " + day.getDatetime() + 
                " (High: " + day.getMaxTemp() + "°C, Low: " + day.getMinTemp() + "°C)");
        }
        
        forecastAdapter.setForecastDays(threeDayForecast);
        Log.d(TAG, "Displaying exactly " + threeDayForecast.size() + " days in 3-day forecast");
        
        if (threeDayForecast.size() < 3) {
            Log.w(TAG, "Warning: Only " + threeDayForecast.size() + " days available for 3-day forecast");
        }
    }

    private void displayClimateChart(List<ForecastDay> forecastDays) {
        // Prepare data for the chart (last 7 days)
        ArrayList<Entry> tempEntries = new ArrayList<>();
        ArrayList<Entry> precipEntries = new ArrayList<>();
        final ArrayList<String> dateLabels = new ArrayList<>();
        
        int daysToShow = Math.min(7, forecastDays.size());
        
        for (int i = 0; i < daysToShow; i++) {
            ForecastDay day = forecastDays.get(i);
            
            // Add temperature data
            tempEntries.add(new Entry(i, (float) day.getTemp()));
            
            // Add precipitation data
            precipEntries.add(new Entry(i, (float) day.getPrecipitation()));
            
            // Format date label
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                dateLabels.add(outputFormat.format(inputFormat.parse(day.getDatetime())));
            } catch (Exception e) {
                dateLabels.add("Day " + (i + 1));
            }
        }
        
        // Create datasets
        LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperature (°C)");
        tempDataSet.setColor(Color.parseColor("#FF6B6B"));
        tempDataSet.setCircleColor(Color.parseColor("#FF6B6B"));
        tempDataSet.setLineWidth(2f);
        tempDataSet.setCircleRadius(4f);
        tempDataSet.setDrawValues(true);
        tempDataSet.setValueTextSize(9f);
        tempDataSet.setValueTextColor(Color.parseColor("#333333"));
        
        LineDataSet precipDataSet = new LineDataSet(precipEntries, "Precipitation (mm)");
        precipDataSet.setColor(Color.parseColor("#4ECDC4"));
        precipDataSet.setCircleColor(Color.parseColor("#4ECDC4"));
        precipDataSet.setLineWidth(2f);
        precipDataSet.setCircleRadius(4f);
        precipDataSet.setDrawValues(true);
        precipDataSet.setValueTextSize(9f);
        precipDataSet.setValueTextColor(Color.parseColor("#333333"));
        
        // Combine datasets
        LineData lineData = new LineData(tempDataSet, precipDataSet);
        chartClimate.setData(lineData);
        
        // Customize chart
        chartClimate.getDescription().setEnabled(false);
        chartClimate.setDrawGridBackground(false);
        chartClimate.setTouchEnabled(true);
        chartClimate.setDragEnabled(true);
        chartClimate.setScaleEnabled(true);
        chartClimate.setPinchZoom(true);
        
        // X-Axis
        XAxis xAxis = chartClimate.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < dateLabels.size()) {
                    return dateLabels.get(index);
                }
                return "";
            }
        });
        xAxis.setTextColor(Color.parseColor("#666666"));
        
        // Left Y-Axis (Temperature)
        YAxis leftAxis = chartClimate.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#666666"));
        leftAxis.setDrawGridLines(true);
        
        // Right Y-Axis
        YAxis rightAxis = chartClimate.getAxisRight();
        rightAxis.setEnabled(false);
        
        // Legend
        chartClimate.getLegend().setTextColor(Color.parseColor("#333333"));
        chartClimate.getLegend().setTextSize(12f);
        
        // Refresh chart
        chartClimate.invalidate();
        
        Log.d(TAG, "Climate chart displayed with " + daysToShow + " days");
    }

    private void analyzeDisasterRisks(List<ForecastDay> forecastDays) {
        disasterAlerts.clear();
        long currentTime = System.currentTimeMillis();
        int alertId = 1;
        
        // Analyze upcoming weather for disaster risks
        for (int i = 0; i < Math.min(7, forecastDays.size()); i++) {
            ForecastDay day = forecastDays.get(i);
            
            // Check for heavy rainfall (potential flooding)
            if (day.getPrecipitation() > 50) {
                DisasterAlert alert = new DisasterAlert(
                        String.valueOf(alertId++),
                        "Heavy Rainfall Alert",
                        String.format(Locale.getDefault(), "%.1f mm expected on %s. Risk of flooding.", 
                            day.getPrecipitation(), formatDate(day.getDatetime())),
                        "HIGH",
                        locationName,
                        currentTime,
                        true
                    );
                alert.setCategory("Flooding");
                alert.setSource("Weather Service");
                alert.setExpiresAt(currentTime + 86400000); // 24 hours
                disasterAlerts.add(alert);
            }
            
            // Check for high winds
            if (day.getWindSpeed() > 15) {
                DisasterAlert alert = new DisasterAlert(
                        String.valueOf(alertId++),
                        "High Wind Warning",
                        String.format(Locale.getDefault(), "%.1f m/s winds expected on %s.", 
                            day.getWindSpeed(), formatDate(day.getDatetime())),
                        "MEDIUM",
                        locationName,
                        currentTime,
                        true
                    );
                alert.setExpiresAt(currentTime + 86400000);
                alert.setCategory("Wind");
                alert.setSource("Weather Service");
                disasterAlerts.add(alert);
            }
            
            // Check for extreme temperatures
            if (day.getMaxTemp() > 35) {
                DisasterAlert alert = new DisasterAlert(
                        String.valueOf(alertId++),
                        "Heat Warning",
                        String.format(Locale.getDefault(), "%.0f°C expected on %s. Risk of heat stress.", 
                            day.getMaxTemp(), formatDate(day.getDatetime())),
                        "HIGH",
                        locationName,
                        currentTime,
                        true
                    );
                alert.setExpiresAt(currentTime + 86400000);
                alert.setCategory("Heat");
                alert.setSource("Weather Service");
                disasterAlerts.add(alert);
            }
            
            if (day.getMinTemp() < 0) {
                DisasterAlert alert = new DisasterAlert(
                        String.valueOf(alertId++),
                        "Frost Warning",
                        String.format(Locale.getDefault(), "%.0f°C expected on %s. Protect sensitive crops.", 
                            day.getMinTemp(), formatDate(day.getDatetime())),
                        "MEDIUM",
                        locationName,
                        currentTime,
                        true
                    );
                alert.setExpiresAt(currentTime + 86400000);
                alert.setCategory("Frost");
                alert.setSource("Weather Service");
                disasterAlerts.add(alert);
            }
            
            // Check for drought conditions (low precipitation over multiple days)
            if (i >= 6) {
                double totalPrecip = 0;
                for (int j = 0; j <= 6; j++) {
                    totalPrecip += forecastDays.get(j).getPrecipitation();
                }
                if (totalPrecip < 5) {
                    DisasterAlert alert = new DisasterAlert(
                            String.valueOf(alertId++),
                            "Drought Risk",
                            String.format(Locale.getDefault(), "Very low rainfall (%.1f mm) in the coming week. Consider irrigation.", totalPrecip),
                            "LOW",
                            locationName,
                            currentTime,
                            true
                        );
                    alert.setExpiresAt(currentTime + (7 * 86400000));
                    alert.setCategory("Drought");
                    alert.setSource("Weather Service");
                    disasterAlerts.add(alert);
                }
            }
        }
        
        // Update UI
        alertAdapter.notifyDataSetChanged();
        tvAlertCount.setText(String.valueOf(disasterAlerts.size()));
        tvNoAlerts.setVisibility(disasterAlerts.isEmpty() ? View.VISIBLE : View.GONE);
        rvDisasterAlerts.setVisibility(disasterAlerts.isEmpty() ? View.GONE : View.VISIBLE);
        
        Log.d(TAG, "Disaster risk analysis completed - " + disasterAlerts.size() + " alerts");
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    // ========== ADVANCED FEATURES: SMART FARMING METHODS ==========
    
    // Crop loading moved to CropManagementActivity
    
    private void generateUpcomingTasks(List<ForecastDay> forecastDays) {
        if (forecastDays == null || forecastDays.isEmpty()) {
            tvUpcomingTasks.setText("No weather data available for task planning.");
            return;
        }
        
        
        // Calculate weather data (make final for lambda)
        double tempWeeklyRain = 0;
        int tempRainyDays = 0;
        
        for (int i = 0; i < Math.min(7, forecastDays.size()); i++) {
            ForecastDay day = forecastDays.get(i);
            tempWeeklyRain += day.getPrecipitation();
            if (day.getPrecipitation() > 5) tempRainyDays++;
        }
        
        final double weeklyRain = tempWeeklyRain;
        final int rainyDays = tempRainyDays;
        
        // Check planting window (make final for lambda)
        boolean tempGoodPlantingWeek = true;
        for (int i = 0; i < Math.min(5, forecastDays.size()); i++) {
            ForecastDay day = forecastDays.get(i);
            if (day.getTemp() < 10 || day.getTemp() > 30 || day.getPrecipitation() > 20) {
                tempGoodPlantingWeek = false;
                break;
            }
        }
        
        final boolean goodPlantingWeek = tempGoodPlantingWeek;
        
        // Get crops info
        List<Crop> activeCrops = cropManager.getActiveCrops();
        List<String> cropsList = new ArrayList<>();
        for (Crop crop : activeCrops) {
            int daysUntilHarvest = crop.getDaysUntilHarvest();
            int daysSincePlanting = crop.getDaysSincePlanting();
            cropsList.add(crop.getName() + " (planted " + daysSincePlanting + " days ago, harvest in " + daysUntilHarvest + " days)");
        }
        
        
        // Use simple local tasks generation (no AI)
        String simpleTasks = generateSimpleUpcomingTasks(forecastDays, activeCrops, 
            weeklyRain, rainyDays, goodPlantingWeek);
        tvUpcomingTasks.setText(simpleTasks);
    }
    
    private String generateSimpleUpcomingTasks(List<ForecastDay> forecastDays, List<Crop> activeCrops,
                                                 double weeklyRain, int rainyDays, boolean goodPlantingWeek) {
        StringBuilder tasks = new StringBuilder();
        
        // Harvest ready crops
        List<Crop> readyForHarvest = cropManager.getCropsReadyForHarvest();
        if (!readyForHarvest.isEmpty()) {
            tasks.append("🌾 Harvest ready:\n");
            for (Crop crop : readyForHarvest) {
                int daysLeft = crop.getDaysUntilHarvest();
                if (daysLeft <= 0) {
                    tasks.append("• ").append(crop.getName()).append(" (ready now)\n");
                } else {
                    tasks.append("• ").append(crop.getName()).append(" (").append(daysLeft).append(" days)\n");
                }
            }
            tasks.append("\n");
        }
        
        // Weather-based tasks - simplified
        tasks.append("📅 This week:\n");
        
        if (rainyDays >= 4) {
            tasks.append("• High rainfall - plan indoor work\n");
        } else if (weeklyRain < 5) {
            tasks.append("• Low rainfall - check irrigation\n");
        } else {
            tasks.append("• Normal conditions - routine work\n");
        }
        
        if (goodPlantingWeek) {
            tasks.append("• Good planting conditions\n");
        }
        
        if (activeCrops.isEmpty() && readyForHarvest.isEmpty()) {
            return "✅ No crops planted yet\n\nAdd crops in the 'My Crops' section.";
        }
        
        return tasks.toString().trim();
    }
}

