package com.mzansi.solutions.disasterdetectoralert;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.mzansi.solutions.disasterdetectoralert.utils.SessionManager;

import java.util.Arrays;
import java.util.List;

public class LocationActivity extends AppCompatActivity {

    private static final String COMMUNITY_PREFS_NAME = "location_prefs"; // For community members
    private static final String FARMER_PREFS_NAME = "farmer_location_prefs"; // For farmers
    private static final String KEY_LOCATION_NAME = "location_name";
    private static final String KEY_LOCATION_ADDRESS = "location_address";
    private static final String KEY_LOCATION_LAT = "location_lat";
    private static final String KEY_LOCATION_LNG = "location_lng";

    private CardView cardSearchLocation;
    private CardView cardCurrentLocation;
    private CardView cardSelectedLocation;
    private TextView tvSelectedLocationName;
    private TextView tvSelectedLocationAddress;
    private MaterialButton btnConfirmLocation;
    private ProgressBar progressBar;

    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences sharedPreferences;
    private SessionManager sessionManager;
    private boolean isFarmerMode;

    // Activity result launcher for Places Autocomplete
    private final ActivityResultLauncher<Intent> startAutocomplete = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        onLocationSelected(place);
                    }
                }
            });

    // Activity result launcher for location permission
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.GOOGLE_PLACES_API_KEY);
        }

        // Initialize views
        initializeViews();
        
        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // Check if coming from farmer flow or community member flow
        isFarmerMode = getIntent().getBooleanExtra("is_farmer", sessionManager.isLoggedIn());
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Use farmer prefs if in farmer mode, community prefs otherwise
        String prefsName = isFarmerMode ? FARMER_PREFS_NAME : COMMUNITY_PREFS_NAME;
        sharedPreferences = getSharedPreferences(prefsName, MODE_PRIVATE);
        
        android.util.Log.d("LocationActivity", "Farmer Mode: " + isFarmerMode + ", Using SharedPreferences: " + prefsName);

        // Set click listeners
        setupClickListeners();

        // Check if location is already saved
        checkSavedLocation();
    }

    private void initializeViews() {
        cardSearchLocation = findViewById(R.id.cardSearchLocation);
        cardCurrentLocation = findViewById(R.id.cardCurrentLocation);
        cardSelectedLocation = findViewById(R.id.cardSelectedLocation);
        tvSelectedLocationName = findViewById(R.id.tvSelectedLocationName);
        tvSelectedLocationAddress = findViewById(R.id.tvSelectedLocationAddress);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        progressBar = findViewById(R.id.progressBar);

        // Back button - check if farmer is logged in
        findViewById(R.id.btnBack).setOnClickListener(v -> handleBackPressed());
    }

    private void setupClickListeners() {
        // Search location click
        cardSearchLocation.setOnClickListener(v -> startLocationSearch());

        // Current location click
        cardCurrentLocation.setOnClickListener(v -> requestLocationPermission());

        // Confirm location click
        btnConfirmLocation.setOnClickListener(v -> confirmLocation());
    }

    private void startLocationSearch() {
        // Set the fields to specify which types of place data to return
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Start the autocomplete intent
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startAutocomplete.launch(intent);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        showProgress(true);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        showProgress(false);
                        if (location != null) {
                            // Create a simple location display for current location
                            onCurrentLocationSelected(location);
                        } else {
                            Toast.makeText(LocationActivity.this, getString(R.string.location_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onLocationSelected(Place place) {
        android.util.Log.d("LocationActivity", "onLocationSelected called with place: " + (place != null ? place.getName() : "null"));
        
        if (place != null) {
            String name = place.getName() != null ? place.getName() : "Unknown Location";
            String address = place.getAddress() != null ? place.getAddress() : "Address not available";
            
            android.util.Log.d("LocationActivity", "Place name: " + name + ", Address: " + address);
            
            // Get coordinates from the place
            LatLng latLng = place.getLatLng();
            if (latLng != null) {
                // Store coordinates in a temporary format for saving
                String coordinateAddress = "Lat: " + latLng.latitude + ", Lng: " + latLng.longitude;
                tvSelectedLocationAddress.setText(coordinateAddress);
                android.util.Log.d("LocationActivity", "Coordinates: " + coordinateAddress);
            } else {
                tvSelectedLocationAddress.setText(address);
                android.util.Log.w("LocationActivity", "No coordinates available for place");
            }
            
            tvSelectedLocationName.setText(name);
            cardSelectedLocation.setVisibility(View.VISIBLE);
            
            android.util.Log.d("LocationActivity", "Location selected and UI updated");
        } else {
            android.util.Log.w("LocationActivity", "Place is null in onLocationSelected");
        }
    }

    private void onCurrentLocationSelected(Location location) {
        String name = "Current Location";
        String address = String.format("Lat: %.4f, Lng: %.4f", 
                location.getLatitude(), location.getLongitude());
        
        tvSelectedLocationName.setText(name);
        tvSelectedLocationAddress.setText(address);
        cardSelectedLocation.setVisibility(View.VISIBLE);
    }

    private void confirmLocation() {
        String name = tvSelectedLocationName.getText().toString();
        String address = tvSelectedLocationAddress.getText().toString();
        
        android.util.Log.d("LocationActivity", "Confirming location - Name: " + name + ", Address: " + address);
        
        if (!name.isEmpty() && !address.isEmpty()) {
            // Save to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_LOCATION_NAME, name);
            editor.putString(KEY_LOCATION_ADDRESS, address);
            
            // Save coordinates if available (for current location or Google Places)
            if (address.startsWith("Lat:")) {
                try {
                    String[] parts = address.replace("Lat: ", "").replace("Lng: ", "").split(", ");
                    if (parts.length == 2) {
                        float lat = Float.parseFloat(parts[0]);
                        float lng = Float.parseFloat(parts[1]);
                        editor.putFloat(KEY_LOCATION_LAT, lat);
                        editor.putFloat(KEY_LOCATION_LNG, lng);
                        android.util.Log.d("LocationActivity", "Saved coordinates - Lat: " + lat + ", Lng: " + lng);
                    }
                } catch (NumberFormatException e) {
                    android.util.Log.e("LocationActivity", "Failed to parse coordinates: " + address, e);
                }
            } else {
                android.util.Log.w("LocationActivity", "No coordinates found in address: " + address);
            }
            
            editor.apply();
            
            Toast.makeText(this, getString(R.string.location_saved), Toast.LENGTH_SHORT).show();
            
            // Navigate to appropriate dashboard based on user role
            Intent intent;
            if (isFarmerMode) {
                android.util.Log.d("LocationActivity", "Farmer mode - Navigating to FarmerDashboardActivity");
                intent = new Intent(this, FarmerDashboardActivity.class);
            } else {
                android.util.Log.d("LocationActivity", "Community member mode - Navigating to DashboardActivity");
                intent = new Intent(this, DashboardActivity.class);
            }
            startActivity(intent);
            finish();
        } else {
            android.util.Log.w("LocationActivity", "Cannot confirm location - Name or address is empty");
            Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkSavedLocation() {
        String savedName = sharedPreferences.getString(KEY_LOCATION_NAME, "");
        String savedAddress = sharedPreferences.getString(KEY_LOCATION_ADDRESS, "");
        
        if (!savedName.isEmpty() && !savedAddress.isEmpty()) {
            tvSelectedLocationName.setText(savedName);
            tvSelectedLocationAddress.setText(savedAddress);
            cardSelectedLocation.setVisibility(View.VISIBLE);
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        cardSearchLocation.setEnabled(!show);
        cardCurrentLocation.setEnabled(!show);
    }
    
    private void handleBackPressed() {
        // Check if in farmer mode and logged in
        if (isFarmerMode && sessionManager.isLoggedIn()) {
            // Show logout confirmation dialog
            showLogoutConfirmationDialog();
        } else {
            // Community member - just go back
            finish();
        }
    }
    
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you want to logout? You will need to login again.")
                .setPositiveButton("Yes, Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Logout and navigate to splash
                        logoutAndNavigateToSplash();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Just dismiss dialog, stay on location page
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    
    private void logoutAndNavigateToSplash() {
        android.util.Log.d("LocationActivity", "Logging out farmer");
        
        // Logout user
        sessionManager.logout();
        
        // Show logout message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate to MainActivity (splash) and clear back stack
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Use same logic as back button click
        handleBackPressed();
    }
}
