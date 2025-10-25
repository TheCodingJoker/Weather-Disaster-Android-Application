package com.mzansi.solutions.disasterdetectoralert.models;

import java.util.Date;

public class Crop {
    private String id;
    private String name;
    private String variety;
    private Date plantingDate;
    private Date expectedHarvestDate;
    private String status; // planted, growing, ready, harvested
    private double areaSize; // in hectares
    private String notes;
    private int growingDays;
    
    // Constructor
    public Crop() {}
    
    public Crop(String id, String name, String variety, Date plantingDate, 
                Date expectedHarvestDate, String status, double areaSize, int growingDays) {
        this.id = id;
        this.name = name;
        this.variety = variety;
        this.plantingDate = plantingDate;
        this.expectedHarvestDate = expectedHarvestDate;
        this.status = status;
        this.areaSize = areaSize;
        this.growingDays = growingDays;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getVariety() { return variety; }
    public Date getPlantingDate() { return plantingDate; }
    public Date getExpectedHarvestDate() { return expectedHarvestDate; }
    public String getStatus() { return status; }
    public double getAreaSize() { return areaSize; }
    public String getNotes() { return notes; }
    public int getGrowingDays() { return growingDays; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setVariety(String variety) { this.variety = variety; }
    public void setPlantingDate(Date plantingDate) { this.plantingDate = plantingDate; }
    public void setExpectedHarvestDate(Date expectedHarvestDate) { this.expectedHarvestDate = expectedHarvestDate; }
    public void setStatus(String status) { this.status = status; }
    public void setAreaSize(double areaSize) { this.areaSize = areaSize; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setGrowingDays(int growingDays) { this.growingDays = growingDays; }
    
    // Helper methods
    public int getDaysUntilHarvest() {
        if (expectedHarvestDate == null) return -1;
        long diff = expectedHarvestDate.getTime() - System.currentTimeMillis();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
    
    public int getDaysSincePlanting() {
        if (plantingDate == null) return 0;
        long diff = System.currentTimeMillis() - plantingDate.getTime();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
    
    public int getGrowthProgress() {
        if (growingDays == 0) return 0;
        return (getDaysSincePlanting() * 100) / growingDays;
    }
}




