package com.mzansi.solutions.disasterdetectoralert.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DisasterAlertResponse {
    @SerializedName("alerts")
    private List<DisasterAlert> alerts;
    
    @SerializedName("location")
    private String location;
    
    @SerializedName("last_updated")
    private long lastUpdated;
    
    // Getters
    public List<DisasterAlert> getAlerts() { return alerts; }
    public String getLocation() { return location; }
    public long getLastUpdated() { return lastUpdated; }
}




