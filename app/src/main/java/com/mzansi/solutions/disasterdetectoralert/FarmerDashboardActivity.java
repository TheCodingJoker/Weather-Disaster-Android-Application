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
import com.mzansi.solutions.disasterdetectoralert.adapters.ActiveCropAdapter;
import com.mzansi.solutions.disasterdetectoralert.adapters.FarmingTipAdapter;
import com.mzansi.solutions.disasterdetectoralert.adapters.WeatherForecastAdapter;
import com.mzansi.solutions.disasterdetectoralert.api.ApiClient;
import com.mzansi.solutions.disasterdetectoralert.api.GeminiApiService;
import com.mzansi.solutions.disasterdetectoralert.api.WeatherApiService;
import com.mzansi.solutions.disasterdetectoralert.models.Crop;
import com.mzansi.solutions.disasterdetectoralert.models.FarmingTip;
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
    private ImageView ivWeatherIcon;
    private TextView tvHumidity;
    private TextView tvWindSpeed;
    private TextView tvPrecipitation;
    private RecyclerView rvForecast;
    private LineChart chartClimate;
    private TextView tvDisasterAlerts;
    private FrameLayout loadingOverlay;
    private BottomNavigationView bottomNavigation;
    
    // New AI & Crop Components
    private RecyclerView rvFarmingTips;
    private TextView tvUpcomingTasks;

    // Data
    private SharedPreferences locationPrefs;
    private SharedPreferences apiPrefs;
    private WeatherForecastAdapter forecastAdapter;
    private FarmingTipAdapter farmingTipAdapter;
    private CropManager cropManager;
    private GeminiApiService geminiApiService;
    private String apiKey;
    private double latitude;
    private double longitude;
    private String locationName;
    private List<ForecastDay> weatherForecastData;
    private boolean aiTipsGenerating = false;
    private boolean aiTasksGenerating = false;

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
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        tvPrecipitation = findViewById(R.id.tvPrecipitation);
        rvForecast = findViewById(R.id.rvForecast);
        chartClimate = findViewById(R.id.chartClimate);
        tvDisasterAlerts = findViewById(R.id.tvDisasterAlerts);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // New advanced features views
        rvFarmingTips = findViewById(R.id.rvFarmingTips);
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
        
        Log.d(TAG, "Location: " + locationName + " (" + latitude + ", " + longitude + ")");
    }

    private void setupRecyclerView() {
        // Weather forecast
        forecastAdapter = new WeatherForecastAdapter(new ArrayList<>());
        rvForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvForecast.setAdapter(forecastAdapter);
        
        // Farming tips
        farmingTipAdapter = new FarmingTipAdapter();
        rvFarmingTips.setLayoutManager(new LinearLayoutManager(this));
        rvFarmingTips.setAdapter(farmingTipAdapter);
        
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
            startActivity(new Intent(this, LocationActivity.class));
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
                        
                        // Generate smart farming tips
                        weatherForecastData = weatherData.getForecastDays();
                        generateSmartFarmingTips(weatherForecastData);
                        
                        // Generate upcoming tasks
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
        // Get days 1-3 (skip today which is day 0)
        List<ForecastDay> threeDayForecast = new ArrayList<>();
        int daysToShow = Math.min(4, allDays.size()); // days 1-3 (indices 1,2,3)
        
        for (int i = 1; i < daysToShow; i++) {
            threeDayForecast.add(allDays.get(i));
        }
        
        forecastAdapter.setForecastDays(threeDayForecast);
        Log.d(TAG, "Displaying " + threeDayForecast.size() + " days in forecast");
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
        StringBuilder alertsText = new StringBuilder();
        boolean hasAlerts = false;
        
        // Analyze upcoming weather for disaster risks
        for (int i = 0; i < Math.min(7, forecastDays.size()); i++) {
            ForecastDay day = forecastDays.get(i);
            
            // Check for heavy rainfall (potential flooding)
            if (day.getPrecipitation() > 50) {
                alertsText.append("⚠️ Heavy Rainfall Alert: ")
                        .append(String.format(Locale.getDefault(), "%.1f mm expected", day.getPrecipitation()))
                        .append(" on ").append(formatDate(day.getDatetime()))
                        .append(". Risk of flooding.\n\n");
                hasAlerts = true;
            }
            
            // Check for high winds
            if (day.getWindSpeed() > 15) {
                alertsText.append("🌪️ High Wind Warning: ")
                        .append(String.format(Locale.getDefault(), "%.1f m/s winds expected", day.getWindSpeed()))
                        .append(" on ").append(formatDate(day.getDatetime()))
                        .append(".\n\n");
                hasAlerts = true;
            }
            
            // Check for extreme temperatures
            if (day.getMaxTemp() > 35) {
                alertsText.append("🌡️ Heat Warning: ")
                        .append(String.format(Locale.getDefault(), "%.0f°C expected", day.getMaxTemp()))
                        .append(" on ").append(formatDate(day.getDatetime()))
                        .append(". Risk of heat stress.\n\n");
                hasAlerts = true;
            }
            
            if (day.getMinTemp() < 0) {
                alertsText.append("❄️ Frost Warning: ")
                        .append(String.format(Locale.getDefault(), "%.0f°C expected", day.getMinTemp()))
                        .append(" on ").append(formatDate(day.getDatetime()))
                        .append(". Protect sensitive crops.\n\n");
                hasAlerts = true;
            }
            
            // Check for drought conditions (low precipitation over multiple days)
            if (i >= 6) {
                double totalPrecip = 0;
                for (int j = 0; j <= 6; j++) {
                    totalPrecip += forecastDays.get(j).getPrecipitation();
                }
                if (totalPrecip < 5) {
                    alertsText.append("☀️ Drought Risk: Very low rainfall in the coming week (")
                            .append(String.format(Locale.getDefault(), "%.1f mm", totalPrecip))
                            .append(" total). Consider irrigation.\n\n");
                    hasAlerts = true;
                }
            }
        }
        
        if (hasAlerts) {
            tvDisasterAlerts.setText(alertsText.toString().trim());
            tvDisasterAlerts.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            tvDisasterAlerts.setText("✅ No immediate disaster risks detected. Weather conditions are favorable.");
            tvDisasterAlerts.setTextColor(Color.parseColor("#388E3C"));
        }
        
        Log.d(TAG, "Disaster risk analysis completed");
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
    
    private void generateSmartFarmingTips(List<ForecastDay> forecastDays) {
        if (forecastDays == null || forecastDays.isEmpty()) {
            return;
        }
        
        if (aiTipsGenerating) {
            Log.d(TAG, "AI tips generation already in progress");
            return;
        }
        
        ForecastDay today = forecastDays.get(0);
        
        // Calculate weather data
        double avgTemp = today.getTemp();
        double humidity = today.getHumidity();
        double precipitation = today.getPrecipitation();
        double windSpeed = today.getWindSpeed();
        
        double weeklyRain = 0;
        for (int i = 0; i < Math.min(7, forecastDays.size()); i++) {
            weeklyRain += forecastDays.get(i).getPrecipitation();
        }
        
        // Get active crops info
        List<Crop> activeCrops = cropManager.getActiveCrops();
        StringBuilder cropsInfo = new StringBuilder();
        for (int i = 0; i < activeCrops.size(); i++) {
            if (i > 0) cropsInfo.append(", ");
            cropsInfo.append(activeCrops.get(i).getName());
        }
        
        // Show loading message
        FarmingTip loadingTip = new FarmingTip(
            "🤖 Generating AI Farming Tips",
            "Analyzing current weather conditions and your crops...",
            "general",
            3
        );
        List<FarmingTip> loadingList = new ArrayList<>();
        loadingList.add(loadingTip);
        farmingTipAdapter.setTips(loadingList);
        
        aiTipsGenerating = true;
        Log.d(TAG, "========================================");
        Log.d(TAG, "Requesting AI-powered farming tips from Gemini AI");
        Log.d(TAG, "Location: " + locationName);
        Log.d(TAG, "Temperature: " + avgTemp + "°C");
        Log.d(TAG, "Crops: " + cropsInfo.toString());
        Log.d(TAG, "========================================");
        
        // Call Gemini API
        geminiApiService.generateFarmingTips(
            locationName,
            avgTemp,
            humidity,
            precipitation,
            windSpeed,
            weeklyRain,
            cropsInfo.toString(),
            new GeminiApiService.FarmingTipsCallback() {
                @Override
                public void onSuccess(String aiTips) {
                    runOnUiThread(() -> {
                        aiTipsGenerating = false;
                        Log.d(TAG, "========================================");
                        Log.d(TAG, "✅ AI FARMING TIPS SUCCESS!");
                        Log.d(TAG, "Response length: " + aiTips.length() + " characters");
                        Log.d(TAG, "========================================");
                        
                        // Parse AI response into FarmingTip objects
                        List<FarmingTip> parsedTips = parseAiFarmingTips(aiTips);
                        farmingTipAdapter.setTips(parsedTips);
                        Toast.makeText(FarmerDashboardActivity.this, 
                            "✅ AI farming advice generated!", Toast.LENGTH_LONG).show();
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        aiTipsGenerating = false;
                        Log.e(TAG, "========================================");
                        Log.e(TAG, "❌ AI FARMING TIPS FAILED!");
                        Log.e(TAG, "Error: " + error);
                        Log.e(TAG, "========================================");
                        
                        // Fallback to local tips generation
                        List<FarmingTip> fallbackTips = generateFallbackFarmingTips(forecastDays);
                        farmingTipAdapter.setTips(fallbackTips);
                        Toast.makeText(FarmerDashboardActivity.this, 
                            "⚠️ Gemini AI Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        );
    }
    
    private List<FarmingTip> parseAiFarmingTips(String aiResponse) {
        List<FarmingTip> tips = new ArrayList<>();
        
        // Add the AI response as a single comprehensive tip
        FarmingTip aiTip = new FarmingTip(
            "🤖 AI-Powered Farming Advice",
            aiResponse,
            "ai_generated",
            5
        );
        tips.add(aiTip);
        
        return tips;
    }
    
    private List<FarmingTip> generateFallbackFarmingTips(List<ForecastDay> forecastDays) {
        List<FarmingTip> tips = new ArrayList<>();
        
        ForecastDay today = forecastDays.get(0);
        ForecastDay tomorrow = forecastDays.size() > 1 ? forecastDays.get(1) : null;
        
        double avgTemp = today.getTemp();
        double humidity = today.getHumidity();
        double precipitation = today.getPrecipitation();
        double windSpeed = today.getWindSpeed();
        
        double weeklyRain = 0;
        for (int i = 0; i < Math.min(7, forecastDays.size()); i++) {
            weeklyRain += forecastDays.get(i).getPrecipitation();
        }
        
        // Basic fallback tips
        if (avgTemp >= 15 && avgTemp <= 25 && precipitation < 5) {
            tips.add(new FarmingTip(
                "Good Planting Day",
                "Soil conditions optimal. Ideal for planting.",
                "planting", 5
            ));
        }
        
        if (weeklyRain < 10) {
            tips.add(new FarmingTip(
                "Irrigation Recommended",
                "Low rainfall this week. Plan irrigation.",
                "irrigation", 4
            ));
        }
        
        if (windSpeed > 15) {
            tips.add(new FarmingTip(
                "High Wind Warning",
                "Avoid spraying pesticides today.",
                "pest_control", 5
            ));
        }
        
        if (humidity > 80 && avgTemp > 20 && avgTemp < 30) {
            tips.add(new FarmingTip(
                "Disease Risk",
                "Monitor crops for fungal diseases.",
                "pest_control", 4
            ));
        }
        
        if (tips.isEmpty()) {
            tips.add(new FarmingTip(
                "Normal Conditions",
                "Weather favorable for farming activities.",
                "general", 1
            ));
        }
        
        return tips;
    }
    
    private void generateUpcomingTasks(List<ForecastDay> forecastDays) {
        if (forecastDays == null || forecastDays.isEmpty()) {
            tvUpcomingTasks.setText("No weather data available for task planning.");
            return;
        }
        
        if (aiTasksGenerating) {
            Log.d(TAG, "AI tasks generation already in progress");
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
        
        // Show loading message
        tvUpcomingTasks.setText("🤖 Generating AI task recommendations...\n\nAnalyzing your crops, weather forecast, and farming schedule...");
        
        aiTasksGenerating = true;
        Log.d(TAG, "Requesting AI-powered upcoming tasks from Gemini AI");
        
        // Call Gemini API
        geminiApiService.generateUpcomingTasks(
            locationName,
            cropsList,
            weeklyRain,
            rainyDays,
            goodPlantingWeek,
            new GeminiApiService.UpcomingTasksCallback() {
                @Override
                public void onSuccess(String aiTasks) {
                    runOnUiThread(() -> {
                        aiTasksGenerating = false;
                        Log.d(TAG, "AI upcoming tasks received successfully");
                        
                        tvUpcomingTasks.setText("🤖 AI-Powered Task Plan:\n\n" + aiTasks);
                        Toast.makeText(FarmerDashboardActivity.this, 
                            "AI task recommendations generated!", Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        aiTasksGenerating = false;
                        Log.e(TAG, "AI upcoming tasks error: " + error);
                        
                        // Fallback to local tasks generation
                        String fallbackTasks = generateFallbackUpcomingTasks(forecastDays, activeCrops, 
                            weeklyRain, rainyDays, goodPlantingWeek);
                        tvUpcomingTasks.setText(fallbackTasks);
                        Toast.makeText(FarmerDashboardActivity.this, 
                            "Using offline task recommendations", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        );
    }
    
    private String generateFallbackUpcomingTasks(List<ForecastDay> forecastDays, List<Crop> activeCrops,
                                                 double weeklyRain, int rainyDays, boolean goodPlantingWeek) {
        StringBuilder tasks = new StringBuilder();
        boolean hasTasks = false;
        
        // Harvest ready crops
        List<Crop> readyForHarvest = cropManager.getCropsReadyForHarvest();
        if (!readyForHarvest.isEmpty()) {
            tasks.append("🌾 HARVEST READY:\n");
            for (Crop crop : readyForHarvest) {
                int daysLeft = crop.getDaysUntilHarvest();
                if (daysLeft <= 0) {
                    tasks.append("  • ").append(crop.getName()).append(" - READY NOW!\n");
                } else {
                    tasks.append("  • ").append(crop.getName()).append(" - in ").append(daysLeft).append(" days\n");
                }
            }
            tasks.append("\n");
            hasTasks = true;
        }
        
        // Fertilization needs
        for (Crop crop : activeCrops) {
            int days = crop.getDaysSincePlanting();
            if (days == 30 || days == 60) {
                if (!hasTasks) {
                    tasks.append("💧 FERTILIZATION:\n");
                }
                tasks.append("  • Apply fertilizer to ").append(crop.getName()).append("\n");
                hasTasks = true;
            }
        }
        
        if (hasTasks) {
            tasks.append("\n");
        }
        
        // Weather-based tasks
        tasks.append("📅 WEEK AHEAD:\n");
        
        if (rainyDays >= 4) {
            tasks.append("  • High rainfall expected - plan indoor tasks\n");
        } else if (weeklyRain < 5) {
            tasks.append("  • Dry week ahead - prepare irrigation\n");
        } else {
            tasks.append("  • Mixed conditions - flexible schedule\n");
        }
        
        if (goodPlantingWeek) {
            tasks.append("  • Optimal planting window - next 5 days\n");
        }
        
        if (!hasTasks && tasks.length() == 0) {
            tasks.append("✅ No urgent tasks\n\nContinue routine maintenance and monitoring.");
        }
        
        return tasks.toString();
    }
}

