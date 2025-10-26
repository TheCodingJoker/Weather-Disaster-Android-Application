package com.mzansi.solutions.disasterdetectoralert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.mzansi.solutions.disasterdetectoralert.adapters.ForecastDayAdapter;
import com.mzansi.solutions.disasterdetectoralert.api.ApiClient;
import com.mzansi.solutions.disasterdetectoralert.api.GeminiApiService;
import com.mzansi.solutions.disasterdetectoralert.api.WeatherApiService;
import com.mzansi.solutions.disasterdetectoralert.models.ClothingSuggestion;
import com.mzansi.solutions.disasterdetectoralert.models.ForecastDay;
import com.mzansi.solutions.disasterdetectoralert.models.ForecastResponse;
import com.mzansi.solutions.disasterdetectoralert.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastActivity extends AppCompatActivity implements ForecastDayAdapter.OnClothingGenerateListener {
    private static final String WEATHERBIT_API_KEY = BuildConfig.WEATHERBIT_API_KEY; // Weatherbit API key
    private static final String KEY_WEATHERBIT_API_KEY = "weatherbit_api_key";
    
    // SharedPreferences keys
    private static final String COMMUNITY_PREFS_NAME = "location_prefs";
    private static final String FARMER_PREFS_NAME = "farmer_location_prefs";
    private static final String KEY_LOCATION_LAT = "location_lat";
    private static final String KEY_LOCATION_LNG = "location_lng";
    private static final String KEY_LOCATION_NAME = "location_name";
    
    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewForecast;
    private LinearLayout layoutLoading, layoutError;
    private MaterialButton btnRetry;
    private BottomNavigationView bottomNavigation;
    
    private ForecastDayAdapter forecastAdapter;
    private List<ForecastDay> forecastDays;
    private List<ClothingSuggestion> clothingSuggestions;
    private WeatherApiService weatherApiService;
    private GeminiApiService geminiApiService;
    private SharedPreferences sharedPreferences;
    private SessionManager sessionManager;
    private boolean isFarmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        
        initializeViews();
        initializeServices();
        setupToolbar();
        setupBottomNavigation();
        
        // Load forecast data after a short delay to ensure UI is ready
        recyclerViewForecast.post(() -> loadForecastData());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewForecast = findViewById(R.id.recyclerViewForecast);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutError = findViewById(R.id.layoutError);
        btnRetry = findViewById(R.id.btnRetry);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Initialize lists
        forecastDays = new ArrayList<>();
        clothingSuggestions = new ArrayList<>();
        
        // Setup RecyclerView
        forecastAdapter = new ForecastDayAdapter();
        forecastAdapter.setClothingGenerateListener(this);
        recyclerViewForecast.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewForecast.setAdapter(forecastAdapter);
        
        // Setup retry button
        btnRetry.setOnClickListener(v -> loadForecastData());
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> showExitDialog());
        
        // Set location name in toolbar subtitle
        String locationName = sharedPreferences.getString(KEY_LOCATION_NAME, "Your Location");
        toolbar.setSubtitle(locationName);
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

    private void setupBottomNavigation() {
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
                Intent intent = new Intent(this, AlertsActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_forecast) {
                // Already on forecast
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
        
        // Set forecast as selected
        bottomNavigation.setSelectedItemId(R.id.nav_forecast);
    }

    private void initializeServices() {
        weatherApiService = ApiClient.getWeatherService();
        geminiApiService = new GeminiApiService();
        
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
        
        android.util.Log.d("ForecastActivity", "Using SharedPreferences: " + prefsName + " (Farmer: " + isFarmer + ")");
        
        // Save API key to SharedPreferences if not already saved
        if (!sharedPreferences.contains(KEY_WEATHERBIT_API_KEY)) {
            sharedPreferences.edit()
                    .putString(KEY_WEATHERBIT_API_KEY, WEATHERBIT_API_KEY)
                    .apply();
            android.util.Log.d("ForecastActivity", "API key saved to SharedPreferences");
        }
    }

    private void loadForecastData() {
        showLoading();
        
        // Get saved location (use defaults if not found, but don't block the user)
        double lat = (double) sharedPreferences.getFloat(KEY_LOCATION_LAT, -33.9249f); // Default to Cape Town
        double lng = (double) sharedPreferences.getFloat(KEY_LOCATION_LNG, 18.4241f);
        
        // Get API key from SharedPreferences or use default
        String apiKey = sharedPreferences.getString(KEY_WEATHERBIT_API_KEY, WEATHERBIT_API_KEY);
        
        android.util.Log.d("ForecastActivity", "Using location - Lat: " + lat + ", Lng: " + lng);
        
        // Debug: Log location and API key
        android.util.Log.d("ForecastActivity", "Using location: " + lat + ", " + lng);
        android.util.Log.d("ForecastActivity", "API Key: " + apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
        
        // Get location name for display
        String locationName = sharedPreferences.getString(KEY_LOCATION_NAME, "Your Location");
        android.util.Log.d("ForecastActivity", "Location name: " + locationName);
        
        // Validate API key
        if (apiKey == null || apiKey.trim().isEmpty()) {
            android.util.Log.e("ForecastActivity", "API key is null or empty");
            showError("Weather API key not configured. Please check settings.");
            return;
        }
        
        // Check if API key looks valid (basic validation)
        if (apiKey.length() < 20) {
            android.util.Log.w("ForecastActivity", "API key seems too short: " + apiKey.length() + " characters");
        }
        
        // Fetch 7-day forecast
        android.util.Log.d("ForecastActivity", "Making API call to Weatherbit...");
        android.util.Log.d("ForecastActivity", "API URL: https://api.weatherbit.io/v2.0/forecast/daily?lat=" + lat + "&lon=" + lng + "&days=16&units=M&key=" + apiKey.substring(0, 8) + "...");
        android.util.Log.d("ForecastActivity", "Request parameters - Lat: " + lat + ", Lng: " + lng + ", Days: 16, Units: M");
        
        // Try requesting 16 days to see what the API actually returns
        Call<ForecastResponse> call = weatherApiService.getForecast7Day(
                lat,
                lng,
                16, // Try 16 days (maximum allowed)
                "M", // Metric units
                apiKey
        );
        
        call.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                // Run UI updates on main thread
                runOnUiThread(() -> {
                    android.util.Log.d("ForecastActivity", "API Response received - Success: " + response.isSuccessful() + ", Code: " + response.code());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ForecastResponse forecastResponse = response.body();
                        android.util.Log.d("ForecastActivity", "Forecast response body received");
                        
                        if (forecastResponse.getForecastDays() != null && !forecastResponse.getForecastDays().isEmpty()) {
                            List<ForecastDay> allDays = forecastResponse.getForecastDays();
                            android.util.Log.d("ForecastActivity", "Loaded " + allDays.size() + " forecast days from API");
                            
                            // Log all days for debugging
                            for (int i = 0; i < allDays.size(); i++) {
                                ForecastDay day = allDays.get(i);
                                android.util.Log.d("ForecastActivity", "Day " + (i + 1) + ": " + day.getDatetime() + 
                                    ", Max: " + day.getMaxTemp() + "°C, Min: " + day.getMinTemp() + "°C, " +
                                    "Weather: " + day.getWeather().getDescription());
                            }
                            
                            // Take only the first 7 days for display
                            if (allDays.size() >= 7) {
                                forecastDays = allDays.subList(0, 7);
                                android.util.Log.d("ForecastActivity", "Displaying first 7 days out of " + allDays.size() + " available");
                            } else {
                                forecastDays = allDays;
                                android.util.Log.w("ForecastActivity", "Warning: Only received " + allDays.size() + " days instead of 7. This might be due to API limitations or subscription plan.");
                                
                                // Show a toast message to inform the user
                                runOnUiThread(() -> {
                                    Toast.makeText(ForecastActivity.this, 
                                        "Note: Only " + allDays.size() + " days available. Upgrade to premium for 7+ day forecasts.", 
                                        Toast.LENGTH_LONG).show();
                                });
                            }
                            
                            forecastAdapter.setForecastDays(forecastDays);
                            showContent();
                        } else {
                            // Debug: Log empty response
                            android.util.Log.e("ForecastActivity", "Empty forecast data received - Days: " + 
                                (forecastResponse.getForecastDays() != null ? forecastResponse.getForecastDays().size() : "null"));
                            showError("Failed to load forecast data. Please try again.");
                        }
                    } else {
                        // Debug: Log API error
                        android.util.Log.e("ForecastActivity", "API Error: " + response.code() + " - " + response.message());
                        
                        String errorMessage = "Failed to load forecast data";
                        if (response.code() == 401) {
                            errorMessage = "Invalid API key. Please check your Weatherbit API configuration.";
                        } else if (response.code() == 403) {
                            errorMessage = "API access denied. Please check your subscription.";
                        } else if (response.code() == 429) {
                            errorMessage = "API rate limit exceeded. Please try again later.";
                            android.util.Log.e("ForecastActivity", "Rate limit exceeded - too many requests");
                        } else if (response.code() >= 500) {
                            errorMessage = "Weather service temporarily unavailable. Please try again later.";
                        }
                        
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                android.util.Log.e("ForecastActivity", "Error body: " + errorBody);
                            } catch (Exception e) {
                                android.util.Log.e("ForecastActivity", "Error reading error body: " + e.getMessage());
                            }
                        }
                        
                        showError(errorMessage);
                    }
                });
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                // Debug: Log network error
                android.util.Log.e("ForecastActivity", "Network error: " + t.getMessage());
                t.printStackTrace();
                
                // Run UI updates on main thread
                runOnUiThread(() -> {
                    // Show specific error based on exception type
                    String errorMessage;
                    if (t instanceof java.net.UnknownHostException) {
                        errorMessage = "No internet connection. Please check your network and try again.";
                    } else if (t instanceof java.net.SocketTimeoutException) {
                        errorMessage = "Request timed out. Please check your connection and try again.";
                    } else if (t instanceof java.net.ConnectException) {
                        errorMessage = "Cannot connect to weather service. Please try again later.";
                    } else {
                        errorMessage = "Network error. Please check your internet connection and try again.";
                    }
                    showError(errorMessage);
                });
            }
        });
    }


    @Override
    public void onGenerateClothing(int position, ForecastDay forecastDay) {
        if (position >= forecastDays.size()) return;
        
        // Show loading state for this specific item
        ForecastDayAdapter.ForecastViewHolder holder = 
                (ForecastDayAdapter.ForecastViewHolder) recyclerViewForecast.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            holder.showClothingLoading();
        }
        
        // Generate clothing suggestion using Gemini AI
        String weatherCondition = forecastDay.getWeather().getDescription();
        String temperature = String.format("%.0f°C", forecastDay.getMaxTemp());
        String location = sharedPreferences.getString(KEY_LOCATION_NAME, "Your Location");
        
        // Debug: Log parameters being sent to Gemini
        android.util.Log.d("ForecastActivity", "Generating clothing suggestion for:");
        android.util.Log.d("ForecastActivity", "Weather: " + weatherCondition);
        android.util.Log.d("ForecastActivity", "Temperature: " + temperature);
        android.util.Log.d("ForecastActivity", "Precipitation: " + forecastDay.getPrecipitationProbability() + "%");
        android.util.Log.d("ForecastActivity", "Wind Speed: " + forecastDay.getWindSpeed() + " km/h");
        android.util.Log.d("ForecastActivity", "Location: " + location);
        
        geminiApiService.generateClothingSuggestion(
                weatherCondition,
                temperature,
                forecastDay.getPrecipitationProbability(),
                forecastDay.getWindSpeed(),
                location,
                new GeminiApiService.ClothingSuggestionCallback() {
                    @Override
                    public void onSuccess(String suggestion) {
                        // Run UI updates on main thread
                        runOnUiThread(() -> {
                            // Create clothing suggestion object
                            ClothingSuggestion clothingSuggestion = new ClothingSuggestion(
                                    forecastDay.getDayOfWeek(),
                                    weatherCondition,
                                    temperature,
                                    suggestion
                            );
                            
                            // Add to suggestions list
                            forecastAdapter.addClothingSuggestion(position, clothingSuggestion);
                            
                            // Hide loading state
                            if (holder != null) {
                                holder.hideClothingLoading();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // Run UI updates on main thread
                        runOnUiThread(() -> {
                            // Hide loading state
                            if (holder != null) {
                                holder.hideClothingLoading();
                            }
                            
                            // Log the error for debugging
                            android.util.Log.e("ForecastActivity", "Clothing suggestion error: " + error);
                            
                            // Show fallback suggestion
                            String fallbackSuggestion = createFallbackClothingSuggestion(weatherCondition, temperature);
                            ClothingSuggestion fallbackClothing = new ClothingSuggestion(
                                    forecastDay.getDayOfWeek(),
                                    weatherCondition,
                                    temperature,
                                    fallbackSuggestion
                            );
                            forecastAdapter.addClothingSuggestion(position, fallbackClothing);
                            
                            Toast.makeText(ForecastActivity.this, 
                                    "⚠️ AI Error: " + error + "\nUsing offline suggestion", Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private String createFallbackClothingSuggestion(String weatherCondition, String temperature) {
        // Simple fallback clothing suggestions based on weather
        if (weatherCondition.toLowerCase().contains("rain") || weatherCondition.toLowerCase().contains("storm")) {
            return "Wear waterproof jacket and pants, waterproof boots, and bring an umbrella. Layer with warm clothing underneath.";
        } else if (weatherCondition.toLowerCase().contains("sunny") || weatherCondition.toLowerCase().contains("clear")) {
            return "Wear light, breathable clothing like cotton t-shirt and shorts. Don't forget sunglasses, hat, and sunscreen.";
        } else if (weatherCondition.toLowerCase().contains("cloudy") || weatherCondition.toLowerCase().contains("overcast")) {
            return "Wear comfortable layers - light shirt with a light jacket or sweater. Jeans or comfortable pants work well.";
        } else if (weatherCondition.toLowerCase().contains("cold") || weatherCondition.toLowerCase().contains("freezing")) {
            return "Layer up with thermal underwear, warm sweater, heavy jacket, gloves, scarf, and warm boots.";
        } else {
            return "Wear comfortable, weather-appropriate clothing. Consider layering for temperature changes throughout the day.";
        }
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        recyclerViewForecast.setVisibility(View.GONE);
    }

    private void showContent() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        recyclerViewForecast.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        recyclerViewForecast.setVisibility(View.GONE);
        
        // Show the error message as a toast
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
