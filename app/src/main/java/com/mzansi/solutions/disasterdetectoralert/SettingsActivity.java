package com.mzansi.solutions.disasterdetectoralert;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.mzansi.solutions.disasterdetectoralert.utils.SessionManager;

public class SettingsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    
    private static final String PREFS_NAME = "location_prefs";
    private static final String KEY_LOCATION_NAME = "location_name";
    private static final String KEY_LOCATION_ADDRESS = "location_address";
    private static final String KEY_LOCATION_LAT = "location_lat";
    private static final String KEY_LOCATION_LNG = "location_lng";
    private static final String KEY_WEATHERBIT_API_KEY = "weatherbit_api_key";
    private static final String WEATHERBIT_API_KEY = BuildConfig.WEATHERBIT_API_KEY; // Weatherbit API key

    private SharedPreferences sharedPreferences;

    // Views
    private MaterialToolbar toolbar;
    private TextView tvCurrentLocationName;
    private TextView tvCurrentLocationAddress;
    private TextView tvCurrentLocationCoords;
    private MaterialButton btnChangeLocation;
    private MaterialButton btnClearData;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // User is not logged in, redirect to appropriate login
            Intent intent = new Intent(this, CommunityLoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Setup bottom navigation
        setupBottomNavigation();

        // Load current settings
        loadCurrentSettings();

        // Setup click listeners
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload settings when returning to this activity (e.g., after changing location)
        loadCurrentSettings();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCurrentLocationName = findViewById(R.id.tvCurrentLocationName);
        tvCurrentLocationAddress = findViewById(R.id.tvCurrentLocationAddress);
        tvCurrentLocationCoords = findViewById(R.id.tvCurrentLocationCoords);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnClearData = findViewById(R.id.btnClearData);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
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
                    SharedPreferences communityPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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
                Intent intent = new Intent(this, DashboardActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_alerts) {
                Intent intent = new Intent(this, AlertsActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_forecast) {
                Intent intent = new Intent(this, ForecastActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Already on settings
                return true;
            }
            return false;
        });

        // Set settings as selected
        bottomNavigation.setSelectedItemId(R.id.nav_settings);
    }

    private void setupClickListeners() {
        btnChangeLocation.setOnClickListener(v -> changeLocation());
        btnClearData.setOnClickListener(v -> showClearDataDialog());
    }

    private void loadCurrentSettings() {
        // Load location settings
        String locationName = sharedPreferences.getString(KEY_LOCATION_NAME, "");
        String locationAddress = sharedPreferences.getString(KEY_LOCATION_ADDRESS, "");
        float lat = sharedPreferences.getFloat(KEY_LOCATION_LAT, 0f);
        float lng = sharedPreferences.getFloat(KEY_LOCATION_LNG, 0f);

        android.util.Log.d("SettingsActivity", "Loading settings - Name: " + locationName + 
            ", Address: " + locationAddress + ", Lat: " + lat + ", Lng: " + lng);

        if (!locationName.isEmpty()) {
            tvCurrentLocationName.setText(locationName);
            
            if (!locationAddress.isEmpty()) {
                tvCurrentLocationAddress.setText(locationAddress);
            }
            
            if (lat != 0f && lng != 0f) {
                String coords = String.format("Lat: %.4f, Lng: %.4f", lat, lng);
                tvCurrentLocationCoords.setText(coords);
            }
        } else {
            tvCurrentLocationName.setText(R.string.no_location_selected);
            tvCurrentLocationAddress.setText("");
            tvCurrentLocationCoords.setText("");
        }
    }

    private void changeLocation() {
        android.util.Log.d("SettingsActivity", "Change location button clicked");
        
        // Navigate to LocationActivity to select a new location
        Intent intent = new Intent(this, LocationActivity.class);
        intent.putExtra("is_farmer", false); // Community member
        startActivity(intent);
        
        // Don't finish this activity so user can come back to settings
        Toast.makeText(this, "Select your new location", Toast.LENGTH_SHORT).show();
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_data_title)
                .setMessage(R.string.clear_data_message)
                .setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearAppData();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void clearAppData() {
        android.util.Log.d("SettingsActivity", "Clearing all app data");
        
        // Logout if farmer is logged in
        if (sessionManager.isLoggedIn()) {
            sessionManager.logout();
            android.util.Log.d("SettingsActivity", "Farmer logged out");
        }
        
        // Clear all SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        
        Toast.makeText(this, R.string.data_cleared, Toast.LENGTH_LONG).show();
        
        // Reload settings to show cleared state
        loadCurrentSettings();
        
        // Navigate to MainActivity (splash) to start fresh
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

