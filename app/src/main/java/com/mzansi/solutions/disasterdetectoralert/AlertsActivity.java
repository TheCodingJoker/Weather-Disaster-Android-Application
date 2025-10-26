package com.mzansi.solutions.disasterdetectoralert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.mzansi.solutions.disasterdetectoralert.adapters.DetailedAlertAdapter;
import com.mzansi.solutions.disasterdetectoralert.api.ApiClient;
import com.mzansi.solutions.disasterdetectoralert.api.WeatherApiService;
import com.mzansi.solutions.disasterdetectoralert.services.RealTimeDisasterAlertService;
import com.mzansi.solutions.disasterdetectoralert.models.DetailedAlert;
import com.mzansi.solutions.disasterdetectoralert.models.DisasterAlert;
import com.mzansi.solutions.disasterdetectoralert.models.WeatherData;
import com.mzansi.solutions.disasterdetectoralert.models.WeatherResponse;
import com.mzansi.solutions.disasterdetectoralert.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertsActivity extends AppCompatActivity {

    private static final String COMMUNITY_PREFS_NAME = "location_prefs";
    private static final String FARMER_PREFS_NAME = "farmer_location_prefs";
    private static final String KEY_LOCATION_NAME = "location_name";
    private static final String KEY_LOCATION_LAT = "location_lat";
    private static final String KEY_LOCATION_LNG = "location_lng";

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private SessionManager sessionManager;
    private boolean isFarmer;

    // Views
    private RecyclerView rvDetailedAlerts;
    private TextView tvNoAlerts;
    private MaterialButton btnRefreshAlerts;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;

    // Adapters
    private DetailedAlertAdapter alertAdapter;

    // Data
    private List<DetailedAlert> detailedAlerts;

    // API Services
    private WeatherApiService weatherApiService;
    
    // Constants for API
    private static final String WEATHERBIT_API_KEY = BuildConfig.WEATHERBIT_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alerts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SharedPreferences
        // Check if user is logged in
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            // User is not logged in, redirect to appropriate login
            Intent intent = new Intent(this, CommunityLoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // Check if user is a farmer
        isFarmer = sessionManager.isFarmer();
        
        // Use appropriate SharedPreferences
        String prefsName = isFarmer ? FARMER_PREFS_NAME : COMMUNITY_PREFS_NAME;
        sharedPreferences = getSharedPreferences(prefsName, MODE_PRIVATE);
        
        android.util.Log.d("AlertsActivity", "Using SharedPreferences: " + prefsName + " (Farmer: " + isFarmer + ")");

        // Initialize views
        initializeViews();
        
        // Initialize data
        initializeData();
        
        // Initialize API services
        weatherApiService = ApiClient.getClient().create(WeatherApiService.class);
        
        // Setup adapters
        setupAdapters();
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Setup click listeners
        setupClickListeners();
        
        // Load alerts
        loadDetailedAlerts();
    }

    private void initializeViews() {
        rvDetailedAlerts = findViewById(R.id.rvDetailedAlerts);
        tvNoAlerts = findViewById(R.id.tvNoAlerts);
        btnRefreshAlerts = findViewById(R.id.btnRefreshAlerts);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
                    
                    // Clear community member location preferences
                    SharedPreferences communityPrefs = getSharedPreferences(COMMUNITY_PREFS_NAME, MODE_PRIVATE);
                    communityPrefs.edit().clear().apply();
                    
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

    private void initializeData() {
        detailedAlerts = new ArrayList<>();
    }

    private void setupAdapters() {
        // Alerts adapter
        alertAdapter = new DetailedAlertAdapter(detailedAlerts);
        rvDetailedAlerts.setLayoutManager(new LinearLayoutManager(this));
        rvDetailedAlerts.setAdapter(alertAdapter);
    }

    private void setupBottomNavigation() {
        // Set the alerts icon as selected
        bottomNavigation.setSelectedItemId(R.id.nav_alerts);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                // Navigate to appropriate dashboard based on user type
                Class<?> dashboardClass = isFarmer ? FarmerDashboardActivity.class : DashboardActivity.class;
                Intent intent = new Intent(this, dashboardClass);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_alerts) {
                // Already on alerts
                return true;
            } else if (itemId == R.id.nav_forecast) {
                Intent intent = new Intent(this, ForecastActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Navigate to settings screen
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        btnRefreshAlerts.setOnClickListener(v -> loadDetailedAlerts());
    }

    private void loadDetailedAlerts() {
        showProgress(true);
        
        // Load real-time detailed alerts
        detailedAlerts.clear();
        
        // Get current weather data to generate real-time alerts
        loadCurrentWeatherAndGenerateAlerts();
    }
    
    private void loadCurrentWeatherAndGenerateAlerts() {
        showProgress(true);
        detailedAlerts.clear();
        
        // Get saved location
        float lat = sharedPreferences.getFloat(KEY_LOCATION_LAT, -33.9249f); // Default to Cape Town
        float lng = sharedPreferences.getFloat(KEY_LOCATION_LNG, 18.4241f);
        String location = sharedPreferences.getString(KEY_LOCATION_NAME, "Your Location");
        
        android.util.Log.d("AlertsActivity", "Fetching REAL weather data - Lat: " + lat + ", Lng: " + lng + ", Name: " + location);
        
        // Fetch REAL weather data from API to generate alerts
        weatherApiService.getCurrentWeather(lat, lng, WEATHERBIT_API_KEY, "M")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse weatherResponse = response.body();
                            if (weatherResponse.getData() != null && !weatherResponse.getData().isEmpty()) {
                                WeatherData weatherData = weatherResponse.getData().get(0);
                                
                                android.util.Log.d("AlertsActivity", "Weather data received - Temp: " + weatherData.getTemperature() + 
                                        ", Wind: " + weatherData.getWindSpeed() + ", Humidity: " + weatherData.getHumidity());
                                
                                // Generate real-time detailed alerts based on REAL weather conditions
                                List<DisasterAlert> realTimeAlerts = RealTimeDisasterAlertService.generateDetailedAlerts(weatherData, location);
                                
                                // Convert DisasterAlert to DetailedAlert
                                for (DisasterAlert alert : realTimeAlerts) {
                                    DetailedAlert detailedAlert = new DetailedAlert(
                                            alert.getId(),
                                            alert.getTitle(),
                                            alert.getDescription(),
                                            alert.getSeverity(),
                                            alert.getLocation(),
                                            alert.getLocation() + " Area",
                                            alert.getSource() != null ? alert.getSource() : "SA Weather Service",
                                            alert.getDescription(), // Use description as instructions
                                            calculateDuration(alert.getExpiresAt() - alert.getTimestamp()),
                                            alert.getTimestamp(),
                                            alert.getExpiresAt(),
                                            alert.getTimestamp(),
                                            alert.isActive(),
                                            alert.getCategory() != null ? alert.getCategory() : "Weather"
                                    );
                                    detailedAlerts.add(detailedAlert);
                                }
                                
                                android.util.Log.d("AlertsActivity", "Generated " + detailedAlerts.size() + " real-time detailed alerts from REAL weather data");
                                
                                runOnUiThread(() -> {
                                    alertAdapter.notifyDataSetChanged();
                                    tvNoAlerts.setVisibility(detailedAlerts.isEmpty() ? View.VISIBLE : View.GONE);
                                    showProgress(false);
                                });
                            } else {
                                android.util.Log.e("AlertsActivity", "Weather data is empty");
                                runOnUiThread(() -> {
                                    Toast.makeText(AlertsActivity.this, "Unable to load weather data", Toast.LENGTH_SHORT).show();
                                    showProgress(false);
                                });
                            }
                        } else {
                            android.util.Log.e("AlertsActivity", "Weather API failed - Code: " + response.code());
                            runOnUiThread(() -> {
                                Toast.makeText(AlertsActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
                                showProgress(false);
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        android.util.Log.e("AlertsActivity", "Weather API call failed: " + t.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(AlertsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        });
                    }
                });
    }
    
    
    private String calculateDuration(long durationMs) {
        long hours = durationMs / (1000 * 60 * 60);
        if (hours > 0) {
            return hours + " hours";
        } else {
            long minutes = durationMs / (1000 * 60);
            return minutes + " minutes";
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        // You could also show an error state in the UI here
    }
}
