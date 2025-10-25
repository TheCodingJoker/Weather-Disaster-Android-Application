package com.mzansi.solutions.disasterdetectoralert.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mzansi.solutions.disasterdetectoralert.models.Crop;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CropManager {
    private static final String PREFS_NAME = "farmer_crops_prefs";
    private static final String KEY_CROPS = "crops_list";
    
    private SharedPreferences prefs;
    private Gson gson;
    
    public CropManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    // Add a new crop
    public void addCrop(Crop crop) {
        List<Crop> crops = getAllCrops();
        if (crop.getId() == null || crop.getId().isEmpty()) {
            crop.setId(UUID.randomUUID().toString());
        }
        crops.add(crop);
        saveCrops(crops);
    }
    
    // Get all crops
    public List<Crop> getAllCrops() {
        String json = prefs.getString(KEY_CROPS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Crop>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    // Get active crops (planted or growing)
    public List<Crop> getActiveCrops() {
        List<Crop> allCrops = getAllCrops();
        List<Crop> activeCrops = new ArrayList<>();
        for (Crop crop : allCrops) {
            if ("planted".equals(crop.getStatus()) || "growing".equals(crop.getStatus())) {
                activeCrops.add(crop);
            }
        }
        return activeCrops;
    }
    
    // Get crops ready for harvest
    public List<Crop> getCropsReadyForHarvest() {
        List<Crop> allCrops = getAllCrops();
        List<Crop> readyCrops = new ArrayList<>();
        for (Crop crop : allCrops) {
            if ("ready".equals(crop.getStatus()) || crop.getDaysUntilHarvest() <= 7) {
                readyCrops.add(crop);
            }
        }
        return readyCrops;
    }
    
    // Update crop status
    public void updateCrop(Crop crop) {
        List<Crop> crops = getAllCrops();
        for (int i = 0; i < crops.size(); i++) {
            if (crops.get(i).getId().equals(crop.getId())) {
                crops.set(i, crop);
                break;
            }
        }
        saveCrops(crops);
    }
    
    // Delete a crop
    public void deleteCrop(String cropId) {
        List<Crop> crops = getAllCrops();
        crops.removeIf(crop -> crop.getId().equals(cropId));
        saveCrops(crops);
    }
    
    // Save crops list
    private void saveCrops(List<Crop> crops) {
        String json = gson.toJson(crops);
        prefs.edit().putString(KEY_CROPS, json).apply();
    }
    
    // Clear all crops
    public void clearAllCrops() {
        prefs.edit().remove(KEY_CROPS).apply();
    }
    
    // Get total farming area
    public double getTotalFarmingArea() {
        List<Crop> crops = getActiveCrops();
        double total = 0;
        for (Crop crop : crops) {
            total += crop.getAreaSize();
        }
        return total;
    }
}




