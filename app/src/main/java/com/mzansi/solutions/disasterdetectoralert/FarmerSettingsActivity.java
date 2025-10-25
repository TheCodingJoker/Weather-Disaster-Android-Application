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

public class FarmerSettingsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    
    // Farmer-specific preferences
    private static final String FARMER_PREFS_NAME = "farmer_location_prefs";
    private static final String KEY_LOCATION_NAME = "location_name";
    private static final String KEY_LOCATION_ADDRESS = "location_address";
    private static final String KEY_LOCATION_LAT = "location_lat";
    private static final String KEY_LOCATION_LNG = "location_lng";

    private SharedPreferences sharedPreferences;

    // Views
    private MaterialToolbar toolbar;
    private TextView tvCurrentLocationName;
    private TextView tvCurrentLocationAddress;
    private TextView tvCurrentLocationCoords;
    private MaterialButton btnChangeLocation;
    private MaterialButton btnClearData;
    private MaterialButton btnLogout;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_settings);

        sharedPreferences = getSharedPreferences(FARMER_PREFS_NAME, MODE_PRIVATE);
        sessionManager = new SessionManager(this);

        initializeViews();
        setupToolbar();
        setupBottomNavigation();
        loadCurrentSettings();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentSettings();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCurrentLocationName = findViewById(R.id.tvCurrentLocationName);
        tvCurrentLocationAddress = findViewById(R.id.tvCurrentLocationAddress);
        tvCurrentLocationCoords = findViewById(R.id.tvCurrentLocationCoords);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        btnClearData = findViewById(R.id.btnClearData);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);
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
                    sessionManager.logout();
                    
                    // Clear farmer location preferences
                    SharedPreferences farmerPrefs = getSharedPreferences("farmer_location_prefs", MODE_PRIVATE);
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
        bottomNavigation.setSelectedItemId(R.id.nav_settings);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_settings) {
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
            } else if (itemId == R.id.nav_forecast) {
                startActivity(new Intent(this, FarmerForecastActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadCurrentSettings() {
        String locationName = sharedPreferences.getString(KEY_LOCATION_NAME, "Not set");
        String locationAddress = sharedPreferences.getString(KEY_LOCATION_ADDRESS, "No address available");
        float latitude = sharedPreferences.getFloat(KEY_LOCATION_LAT, 0f);
        float longitude = sharedPreferences.getFloat(KEY_LOCATION_LNG, 0f);

        tvCurrentLocationName.setText(locationName);
        tvCurrentLocationAddress.setText(locationAddress);
        
        if (latitude != 0f && longitude != 0f) {
            tvCurrentLocationCoords.setText(String.format("%.4f, %.4f", latitude, longitude));
        } else {
            tvCurrentLocationCoords.setText("No coordinates set");
        }
    }

    private void setupClickListeners() {
        btnChangeLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationActivity.class);
            intent.putExtra("is_farmer", true);
            startActivity(intent);
        });

        btnClearData.setOnClickListener(v -> showClearDataDialog());
        
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("This will clear all your location data and settings. Continue?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    
                    loadCurrentSettings();
                    Toast.makeText(this, "Data cleared successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Logout user and clear session
                    sessionManager.logout();
                    
                    // Clear farmer location preferences as well
                    SharedPreferences farmerPrefs = getSharedPreferences("farmer_location_prefs", MODE_PRIVATE);
                    farmerPrefs.edit().clear().apply();
                    
                    // Redirect to main screen (role selection)
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

