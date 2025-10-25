package com.mzansi.solutions.disasterdetectoralert;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mzansi.solutions.disasterdetectoralert.adapters.CropListAdapter;
import com.mzansi.solutions.disasterdetectoralert.models.Crop;
import com.mzansi.solutions.disasterdetectoralert.utils.CropManager;
import com.mzansi.solutions.disasterdetectoralert.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CropManagementActivity extends AppCompatActivity {

    private CropManager cropManager;
    private RecyclerView rvAllCrops;
    private CropListAdapter cropListAdapter;
    private FloatingActionButton fabAddCrop;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private TextView tvTotalCrops, tvGrowingCrops, tvReadyCrops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_management);

        cropManager = new CropManager(this);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupFab();
        setupBottomNavigation();
        loadCrops();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        rvAllCrops = findViewById(R.id.rvAllCrops);
        fabAddCrop = findViewById(R.id.fabAddCrop);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        tvTotalCrops = findViewById(R.id.tvTotalCrops);
        tvGrowingCrops = findViewById(R.id.tvGrowingCrops);
        tvReadyCrops = findViewById(R.id.tvReadyCrops);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Crops");
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

    private void setupRecyclerView() {
        cropListAdapter = new CropListAdapter(
                cropManager.getAllCrops(),
                this::showEditCropDialog,
                this::deleteCrop
        );
        rvAllCrops.setLayoutManager(new LinearLayoutManager(this));
        rvAllCrops.setAdapter(cropListAdapter);
    }

    private void setupFab() {
        fabAddCrop.setOnClickListener(v -> showAddCropDialog());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_crops);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_crops) {
                return true;
            } else if (itemId == R.id.nav_dashboard) {
                // Navigate to Farmer Dashboard
                startActivity(new Intent(this, FarmerDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_forecast) {
                startActivity(new Intent(this, FarmerForecastActivity.class));
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

    private void loadCrops() {
        List<Crop> crops = cropManager.getAllCrops();
        cropListAdapter.setCrops(crops);
        
        // Update summary statistics
        updateSummaryStats(crops);
        
        // Show/hide empty state
        TextView tvNoCrops = findViewById(R.id.tvNoCrops);
        if (crops.isEmpty()) {
            tvNoCrops.setVisibility(View.VISIBLE);
            rvAllCrops.setVisibility(View.GONE);
        } else {
            tvNoCrops.setVisibility(View.GONE);
            rvAllCrops.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateSummaryStats(List<Crop> crops) {
        int total = crops.size();
        int growing = 0;
        int ready = 0;
        
        for (Crop crop : crops) {
            String status = crop.getStatus().toLowerCase();
            if (status.equals("growing") || status.equals("planted")) {
                growing++;
            } else if (status.equals("ready")) {
                ready++;
            }
        }
        
        tvTotalCrops.setText(String.valueOf(total));
        tvGrowingCrops.setText(String.valueOf(growing));
        tvReadyCrops.setText(String.valueOf(ready));
    }

    private void showAddCropDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_crop, null);

        EditText etCropName = dialogView.findViewById(R.id.etCropName);
        EditText etCropVariety = dialogView.findViewById(R.id.etCropVariety);
        EditText etPlantingDate = dialogView.findViewById(R.id.etPlantingDate);
        EditText etGrowingDays = dialogView.findViewById(R.id.etGrowingDays);
        EditText etAreaSize = dialogView.findViewById(R.id.etAreaSize);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);

        // Setup status spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.crop_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Date picker for planting date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etPlantingDate.setText(sdf.format(calendar.getTime()));
        
        etPlantingDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        etPlantingDate.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Crop")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String name = etCropName.getText().toString().trim();
                String variety = etCropVariety.getText().toString().trim();
                String growingDaysStr = etGrowingDays.getText().toString().trim();
                String areaSizeStr = etAreaSize.getText().toString().trim();
                String status = spinnerStatus.getSelectedItem().toString().toLowerCase();

                if (name.isEmpty()) {
                    etCropName.setError("Crop name required");
                    return;
                }

                if (growingDaysStr.isEmpty()) {
                    etGrowingDays.setError("Growing days required");
                    return;
                }

                try {
                    Date plantingDate = sdf.parse(etPlantingDate.getText().toString());
                    int growingDays = Integer.parseInt(growingDaysStr);
                    double areaSize = areaSizeStr.isEmpty() ? 0 : Double.parseDouble(areaSizeStr);

                    // Calculate expected harvest date
                    Calendar harvestCal = Calendar.getInstance();
                    harvestCal.setTime(plantingDate);
                    harvestCal.add(Calendar.DAY_OF_YEAR, growingDays);
                    Date expectedHarvest = harvestCal.getTime();

                    Crop crop = new Crop(
                            null,
                            name,
                            variety,
                            plantingDate,
                            expectedHarvest,
                            status,
                            areaSize,
                            growingDays
                    );

                    cropManager.addCrop(crop);
                    loadCrops();
                    Toast.makeText(this, "Crop added successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showEditCropDialog(Crop crop) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_crop, null);

        EditText etCropName = dialogView.findViewById(R.id.etCropName);
        EditText etCropVariety = dialogView.findViewById(R.id.etCropVariety);
        EditText etPlantingDate = dialogView.findViewById(R.id.etPlantingDate);
        EditText etGrowingDays = dialogView.findViewById(R.id.etGrowingDays);
        EditText etAreaSize = dialogView.findViewById(R.id.etAreaSize);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);

        // Setup status spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.crop_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Pre-fill data
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etCropName.setText(crop.getName());
        etCropVariety.setText(crop.getVariety());
        etPlantingDate.setText(sdf.format(crop.getPlantingDate()));
        etGrowingDays.setText(String.valueOf(crop.getGrowingDays()));
        etAreaSize.setText(String.valueOf(crop.getAreaSize()));
        
        // Set spinner selection
        String[] statusArray = getResources().getStringArray(R.array.crop_status_array);
        for (int i = 0; i < statusArray.length; i++) {
            if (statusArray[i].equalsIgnoreCase(crop.getStatus())) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        // Date picker for planting date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(crop.getPlantingDate());
        
        etPlantingDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        etPlantingDate.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Crop")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String name = etCropName.getText().toString().trim();
                String variety = etCropVariety.getText().toString().trim();
                String growingDaysStr = etGrowingDays.getText().toString().trim();
                String areaSizeStr = etAreaSize.getText().toString().trim();
                String status = spinnerStatus.getSelectedItem().toString().toLowerCase();

                if (name.isEmpty()) {
                    etCropName.setError("Crop name required");
                    return;
                }

                if (growingDaysStr.isEmpty()) {
                    etGrowingDays.setError("Growing days required");
                    return;
                }

                try {
                    Date plantingDate = sdf.parse(etPlantingDate.getText().toString());
                    int growingDays = Integer.parseInt(growingDaysStr);
                    double areaSize = areaSizeStr.isEmpty() ? 0 : Double.parseDouble(areaSizeStr);

                    // Calculate expected harvest date
                    Calendar harvestCal = Calendar.getInstance();
                    harvestCal.setTime(plantingDate);
                    harvestCal.add(Calendar.DAY_OF_YEAR, growingDays);
                    Date expectedHarvest = harvestCal.getTime();

                    // Update crop object
                    crop.setName(name);
                    crop.setVariety(variety);
                    crop.setPlantingDate(plantingDate);
                    crop.setExpectedHarvestDate(expectedHarvest);
                    crop.setStatus(status);
                    crop.setAreaSize(areaSize);
                    crop.setGrowingDays(growingDays);

                    cropManager.updateCrop(crop);
                    loadCrops();
                    Toast.makeText(this, "Crop updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void deleteCrop(Crop crop) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Crop")
                .setMessage("Are you sure you want to delete " + crop.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    cropManager.deleteCrop(crop.getId());
                    loadCrops();
                    Toast.makeText(this, "Crop deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}



