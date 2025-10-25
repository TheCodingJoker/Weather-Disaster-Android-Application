package com.mzansi.solutions.disasterdetectoralert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mzansi.solutions.disasterdetectoralert.adapters.DisasterAlertAdapter;
import com.mzansi.solutions.disasterdetectoralert.adapters.WeatherForecastAdapter;
import com.mzansi.solutions.disasterdetectoralert.api.ApiClient;
import com.mzansi.solutions.disasterdetectoralert.api.WeatherApiService;
import com.mzansi.solutions.disasterdetectoralert.services.RealTimeDisasterAlertService;
import com.mzansi.solutions.disasterdetectoralert.models.DisasterAlert;
import com.mzansi.solutions.disasterdetectoralert.models.ForecastDay;
import com.mzansi.solutions.disasterdetectoralert.models.ForecastResponse;
import com.mzansi.solutions.disasterdetectoralert.models.WeatherData;
import com.mzansi.solutions.disasterdetectoralert.models.WeatherResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "location_prefs"; // Changed to match ForecastActivity
    private static final String KEY_LOCATION_NAME = "location_name";
    private static final String KEY_LOCATION_LAT = "location_lat";
    private static final String KEY_LOCATION_LNG = "location_lng";
    private static final String WEATHERBIT_API_KEY = BuildConfig.WEATHERBIT_API_KEY; // Weatherbit API key
    private static final String KEY_WEATHERBIT_API_KEY = "weatherbit_api_key"; // Key for storing/retrieving API key from SharedPreferences

    // SharedPreferences
    private SharedPreferences sharedPreferences;

    // Weather Views
    private ImageView ivWeatherIcon;
    private TextView tvTemperature;
    private TextView tvWeatherDescription;
    private TextView tvLocation;
    private TextView tvFeelsLike;
    private TextView tvHumidity;
    private TextView tvWindSpeed;
    private TextView tvUvIndex;
    private TextView tvPressure;
    private TextView tvVisibility;

    // Alerts Views
    private RecyclerView rvAlerts;
    private TextView tvAlertCount;
    private TextView tvNoAlerts;

    // Forecast Views
    private RecyclerView rvForecast;

    // Other Views
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;

    // Adapters
    private DisasterAlertAdapter alertAdapter;
    private WeatherForecastAdapter forecastAdapter;

    // Data
    private List<DisasterAlert> disasterAlerts;
    private List<ForecastDay> forecastData;

    // API Service
    private WeatherApiService weatherApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        initializeViews();
        
        // Initialize data
        initializeData();
        
        // Initialize API service
        weatherApiService = ApiClient.getWeatherService();
        
        // Setup adapters
        setupAdapters();
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Load weather data
        loadWeatherData();
        
        // Load disaster alerts (will be updated when weather data loads)
        loadDisasterAlerts();
        
        // Setup refresh functionality
        setupRefresh();
    }

    private void initializeViews() {
        // Weather views
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherDescription = findViewById(R.id.tvWeatherDescription);
        tvLocation = findViewById(R.id.tvLocation);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        tvUvIndex = findViewById(R.id.tvUvIndex);
        tvPressure = findViewById(R.id.tvPressure);
        tvVisibility = findViewById(R.id.tvVisibility);

        // Alerts views
        rvAlerts = findViewById(R.id.rvAlerts);
        tvAlertCount = findViewById(R.id.tvAlertCount);
        tvNoAlerts = findViewById(R.id.tvNoAlerts);

        // Forecast views
        rvForecast = findViewById(R.id.rvForecast);

        // Other views
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Set up back button to show exit dialog
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
                    // Exit the app
                    finishAffinity();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss dialog
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void initializeData() {
        disasterAlerts = new ArrayList<>();
        forecastData = new ArrayList<>();
    }

    private void setupAdapters() {
        // Alerts adapter
        alertAdapter = new DisasterAlertAdapter(disasterAlerts);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        rvAlerts.setAdapter(alertAdapter);

        // Forecast adapter
        forecastAdapter = new WeatherForecastAdapter(forecastData);
        rvForecast.setLayoutManager(new LinearLayoutManager(this));
        rvForecast.setAdapter(forecastAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                // Already on dashboard
                return true;
            } else if (itemId == R.id.nav_alerts) {
                // Navigate to alerts screen
                Intent intent = new Intent(this, AlertsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_forecast) {
                // Navigate to forecast screen
                Intent intent = new Intent(this, ForecastActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Navigate to settings screen
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadWeatherData() {
        // Check if user has selected a location
        if (!sharedPreferences.contains(KEY_LOCATION_LAT) || !sharedPreferences.contains(KEY_LOCATION_LNG)) {
            // No location selected, redirect to location selection
            android.util.Log.w("DashboardActivity", "No location selected, redirecting to LocationActivity");
            Toast.makeText(this, "Please select your location first", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LocationActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        android.util.Log.d("DashboardActivity", "Location found in SharedPreferences");
        
        double lat = sharedPreferences.getFloat(KEY_LOCATION_LAT, -33.9249f);
        double lng = sharedPreferences.getFloat(KEY_LOCATION_LNG, 18.4241f);

        showProgress(true);

        // Get API key from SharedPreferences or use default (same approach as ForecastActivity)
        String apiKey = sharedPreferences.getString(KEY_WEATHERBIT_API_KEY, WEATHERBIT_API_KEY);
        
        // Load current weather
        android.util.Log.d("DashboardActivity", "Loading current weather for lat: " + lat + ", lng: " + lng);
        android.util.Log.d("DashboardActivity", "Using API key: " + apiKey.substring(0, 8) + "...");
        weatherApiService.getCurrentWeather(lat, lng, apiKey, "M")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        runOnUiThread(() -> {
                            showProgress(false);
                            android.util.Log.d("DashboardActivity", "Weather API response - Success: " + response.isSuccessful() + ", Code: " + response.code());
                            
                            if (response.isSuccessful() && response.body() != null && !response.body().getData().isEmpty()) {
                                WeatherData weatherData = response.body().getData().get(0);
                                android.util.Log.d("DashboardActivity", "Weather data received - Temp: " + weatherData.getTemperature() + "°C, Description: " + weatherData.getWeather().getDescription());
                                updateCurrentWeather(weatherData);
                                // Update disaster alerts based on real weather data
                                updateDisasterAlerts(weatherData);
                            } else {
                                android.util.Log.e("DashboardActivity", "Weather API failed - Code: " + response.code() + ", Message: " + response.message());
                                
                                String errorMessage = "Weather data unavailable";
                                if (response.code() == 429) {
                                    errorMessage = "API rate limit exceeded. Please try again later.";
                                    android.util.Log.e("DashboardActivity", "Rate limit exceeded - too many requests");
                                } else if (response.code() == 401) {
                                    errorMessage = "Invalid API key. Please check configuration.";
                                    android.util.Log.e("DashboardActivity", "Invalid API key");
                                } else if (response.code() == 403) {
                                    errorMessage = "API access denied. Please check your subscription.";
                                    android.util.Log.e("DashboardActivity", "API access denied");
                                }
                                
                                if (response.errorBody() != null) {
                                    try {
                                        String errorBody = response.errorBody().string();
                                        android.util.Log.e("DashboardActivity", "Error body: " + errorBody);
                                    } catch (Exception e) {
                                        android.util.Log.e("DashboardActivity", "Error reading error body: " + e.getMessage());
                                    }
                                }
                                
                                Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                showWeatherError();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        runOnUiThread(() -> {
                            showProgress(false);
                            showWeatherError();
                        });
                    }
                });

        // Load forecast using same method as ForecastActivity
        android.util.Log.d("DashboardActivity", "Loading forecast for lat: " + lat + ", lng: " + lng);
        android.util.Log.d("DashboardActivity", "Using API key for forecast: " + apiKey.substring(0, 8) + "...");
        // Request 16 days (same as ForecastActivity) to ensure consistent data, then display first 3
        weatherApiService.getForecast7Day(lat, lng, 16, "M", apiKey)
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                        runOnUiThread(() -> {
                            android.util.Log.d("DashboardActivity", "Forecast API response - Success: " + response.isSuccessful() + ", Code: " + response.code());
                            
                            if (response.isSuccessful() && response.body() != null) {
                                ForecastResponse forecastResponse = response.body();
                                if (forecastResponse.getForecastDays() != null && !forecastResponse.getForecastDays().isEmpty()) {
                                    List<ForecastDay> allDays = forecastResponse.getForecastDays();
                                    android.util.Log.d("DashboardActivity", "Received " + allDays.size() + " forecast days from API");
                                    
                                    // Take only the first 3 days for dashboard display (summary view)
                                    forecastData.clear();
                                    int daysToShow = Math.min(3, allDays.size());
                                    forecastData.addAll(allDays.subList(0, daysToShow));
                                    forecastAdapter.notifyDataSetChanged();
                                    android.util.Log.d("DashboardActivity", "Displaying first " + daysToShow + " forecast days on dashboard");
                                } else {
                                    android.util.Log.e("DashboardActivity", "Forecast data is empty");
                                }
                            } else {
                                android.util.Log.e("DashboardActivity", "Forecast API failed - Code: " + response.code() + ", Message: " + response.message());
                                
                                String errorMessage = "Forecast data unavailable";
                                if (response.code() == 429) {
                                    errorMessage = "API rate limit exceeded. Please try again later.";
                                    android.util.Log.e("DashboardActivity", "Forecast rate limit exceeded");
                                } else if (response.code() == 401) {
                                    errorMessage = "Invalid API key. Please check configuration.";
                                    android.util.Log.e("DashboardActivity", "Invalid API key for forecast");
                                } else if (response.code() == 403) {
                                    errorMessage = "API access denied. Please check your subscription.";
                                    android.util.Log.e("DashboardActivity", "API access denied for forecast");
                                }
                                
                                if (response.errorBody() != null) {
                                    try {
                                        String errorBody = response.errorBody().string();
                                        android.util.Log.e("DashboardActivity", "Forecast error body: " + errorBody);
                                    } catch (Exception e) {
                                        android.util.Log.e("DashboardActivity", "Error reading forecast error body: " + e.getMessage());
                                    }
                                }
                                
                                Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {
                        android.util.Log.e("DashboardActivity", "Forecast loading failed: " + t.getMessage());
                    }
                });
    }

    private void updateCurrentWeather(WeatherData weatherData) {
        tvTemperature.setText(String.format("%.0f°C", weatherData.getTemperature()));
        tvWeatherDescription.setText(weatherData.getWeather().getDescription());
        tvLocation.setText(weatherData.getCityName() + ", " + weatherData.getCountryCode());
        tvFeelsLike.setText(String.format("%.0f°C", weatherData.getFeelsLike()));
        tvHumidity.setText(String.format("%.0f%%", weatherData.getHumidity()));
        tvWindSpeed.setText(String.format("%.1f km/h", weatherData.getWindSpeed()));
        tvUvIndex.setText(String.format("%.0f", weatherData.getUvIndex()));
        tvPressure.setText(String.format("%.0f hPa", weatherData.getPressure()));
        tvVisibility.setText(String.format("%.1f km", weatherData.getVisibility()));

        // Load weather icon from Weatherbit API
        loadWeatherIcon(weatherData.getWeather().getIconUrl());
    }

    private void loadWeatherIcon(String iconUrl) {
        Glide.with(this)
                .load(iconUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivWeatherIcon);
    }

    private void loadDisasterAlerts() {
        // Load real-time disaster alerts based on current weather
        // This will be called after weather data is loaded
        android.util.Log.d("DashboardActivity", "Loading real-time disaster alerts");
        
        // Get saved location
        double lat = sharedPreferences.getFloat(KEY_LOCATION_LAT, -33.9249f);
        double lng = sharedPreferences.getFloat(KEY_LOCATION_LNG, 18.4241f);
        String location = sharedPreferences.getString(KEY_LOCATION_NAME, "Your Location");
        
        // Generate real-time alerts based on current weather conditions
        // This will be populated when weather data is available
        disasterAlerts.clear();
        alertAdapter.notifyDataSetChanged();
        updateAlertCount();
    }
    
    private void updateDisasterAlerts(WeatherData weatherData) {
        // Generate real-time disaster alerts based on current weather
        String location = sharedPreferences.getString(KEY_LOCATION_NAME, "Your Location");
        List<DisasterAlert> realTimeAlerts = RealTimeDisasterAlertService.generateRealTimeAlerts(weatherData, location);
        
        disasterAlerts.clear();
        disasterAlerts.addAll(realTimeAlerts);
        
        android.util.Log.d("DashboardActivity", "Generated " + realTimeAlerts.size() + " real-time disaster alerts");
        
        alertAdapter.notifyDataSetChanged();
        updateAlertCount();
    }

    private void updateAlertCount() {
        int activeAlerts = 0;
        for (DisasterAlert alert : disasterAlerts) {
            if (alert.isActive()) {
                activeAlerts++;
            }
        }
        tvAlertCount.setText(String.valueOf(activeAlerts));
        tvNoAlerts.setVisibility(activeAlerts == 0 ? View.VISIBLE : View.GONE);
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showWeatherError() {
        Toast.makeText(this, getString(R.string.weather_error), Toast.LENGTH_SHORT).show();
    }
    
    private void setupRefresh() {
        // Add pull-to-refresh functionality
        // This would typically use SwipeRefreshLayout
        android.util.Log.d("DashboardActivity", "Refresh functionality setup - data will refresh automatically");
    }
    
    public void refreshData() {
        // Method to refresh all data
        android.util.Log.d("DashboardActivity", "Refreshing all data");
        loadWeatherData();
    }
}
