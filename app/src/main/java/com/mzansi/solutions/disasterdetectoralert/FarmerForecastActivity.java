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

public class FarmerForecastActivity extends AppCompatActivity implements ForecastDayAdapter.OnClothingGenerateListener {
    private static final String WEATHERBIT_API_KEY = BuildConfig.WEATHERBIT_API_KEY;
    
    // SharedPreferences keys for farmer
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_forecast);
        
        initializeViews();
        initializeServices();
        setupToolbar();
        setupBottomNavigation();
        
        recyclerViewForecast.post(() -> loadForecastData());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewForecast = findViewById(R.id.recyclerViewForecast);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutError = findViewById(R.id.layoutError);
        btnRetry = findViewById(R.id.btnRetry);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        forecastDays = new ArrayList<>();
        clothingSuggestions = new ArrayList<>();
    }

    private void initializeServices() {
        sharedPreferences = getSharedPreferences(FARMER_PREFS_NAME, MODE_PRIVATE);
        weatherApiService = ApiClient.getClient().create(WeatherApiService.class);
        geminiApiService = new GeminiApiService();
        
        forecastAdapter = new ForecastDayAdapter();
        forecastAdapter.setClothingGenerateListener(this);
        recyclerViewForecast.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewForecast.setAdapter(forecastAdapter);
    }

    private void setupToolbar() {
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
                    
                    // Clear farmer location preferences
                    SharedPreferences farmerPrefs = getSharedPreferences(FARMER_PREFS_NAME, MODE_PRIVATE);
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

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_forecast);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_forecast) {
                return true;
            } else if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, FarmerDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_crops) {
                startActivity(new Intent(this, CropManagementActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, FarmerSettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadForecastData() {
        float latitude = sharedPreferences.getFloat(KEY_LOCATION_LAT, -33.9249f);
        float longitude = sharedPreferences.getFloat(KEY_LOCATION_LNG, 18.4241f);
        String locationName = sharedPreferences.getString(KEY_LOCATION_NAME, "Cape Town");

        android.util.Log.d("FarmerForecast", "Loading forecast for: " + locationName + " (" + latitude + ", " + longitude + ")");

        showLoading(true);
        showError(false);

        weatherApiService.getForecast(latitude, longitude, WEATHERBIT_API_KEY, 16, "M")
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            ForecastResponse forecastResponse = response.body();
                            
                            if (forecastResponse.getForecastDays() != null && !forecastResponse.getForecastDays().isEmpty()) {
                                forecastDays.clear();
                                forecastDays.addAll(forecastResponse.getForecastDays());
                                
                                android.util.Log.d("FarmerForecast", "Loaded " + forecastDays.size() + " days of forecast");
                                
                                forecastAdapter.setForecastDays(forecastDays);
                                
                                if (forecastDays.isEmpty()) {
                                    showError(true);
                                }
                            } else {
                                android.util.Log.e("FarmerForecast", "Forecast data is empty");
                                showError(true);
                            }
                        } else {
                            android.util.Log.e("FarmerForecast", "API error: " + response.code());
                            showError(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {
                        showLoading(false);
                        showError(true);
                        android.util.Log.e("FarmerForecast", "Network error: " + t.getMessage());
                        Toast.makeText(FarmerForecastActivity.this, 
                            "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        
        btnRetry.setOnClickListener(v -> loadForecastData());
    }

    @Override
    public void onGenerateClothing(int position, ForecastDay day) {
        if (position < 0 || position >= forecastDays.size()) {
            Toast.makeText(this, "Invalid forecast day", Toast.LENGTH_SHORT).show();
            return;
        }
        android.util.Log.d("FarmerForecast", "Generating clothing suggestions for: " + day.getDatetime());

        geminiApiService.generateClothingSuggestion(
            (day.getWeather() != null && day.getWeather().getDescription() != null) ? day.getWeather().getDescription() : "Unknown",
            String.format("%.1f°C", day.getTemp()),
            day.getPrecipitationProbability(),
            day.getWindSpeed(),
            "Your Location",
            new GeminiApiService.ClothingSuggestionCallback() {
            @Override
            public void onSuccess(String generatedText) {
                runOnUiThread(() -> {
                    List<String> suggestions = parseClothingSuggestions(generatedText);
                    
                    if (!suggestions.isEmpty() && suggestions.size() >= 3) {
                        ClothingSuggestion clothingSuggestion = new ClothingSuggestion(
                            day.getDatetime(),
                            suggestions.size() > 0 ? suggestions.get(0) : "",
                            suggestions.size() > 1 ? suggestions.get(1) : "",
                            suggestions.size() > 2 ? suggestions.get(2) : ""
                        );
                        
                        if (position < clothingSuggestions.size()) {
                            clothingSuggestions.set(position, clothingSuggestion);
                        } else {
                            while (clothingSuggestions.size() < position) {
                                clothingSuggestions.add(null);
                            }
                            clothingSuggestions.add(clothingSuggestion);
                        }
                        
                        forecastAdapter.addClothingSuggestion(position, clothingSuggestion);
                        android.util.Log.d("FarmerForecast", "Clothing suggestions generated successfully");
                    } else {
                        Toast.makeText(FarmerForecastActivity.this, 
                            "Could not generate enough suggestions", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(FarmerForecastActivity.this, 
                        "Error: " + error, Toast.LENGTH_SHORT).show();
                    android.util.Log.e("FarmerForecast", "Gemini API error: " + error);
                });
            }
        });
    }

    private List<String> parseClothingSuggestions(String text) {
        List<String> suggestions = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return suggestions;
        }
        
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            
            trimmed = trimmed.replaceAll("^[\\d\\*\\-•]+\\.?\\s*", "");
            
            if (!trimmed.isEmpty() && trimmed.length() > 5) {
                suggestions.add(trimmed);
            }
        }
        
        return suggestions;
    }

    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewForecast.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(boolean show) {
        layoutError.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewForecast.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}

