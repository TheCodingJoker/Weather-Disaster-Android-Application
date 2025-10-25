package com.mzansi.solutions.disasterdetectoralert.models;

import com.google.gson.annotations.SerializedName;

public class DisasterAlert {
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("severity")
    private String severity;
    
    @SerializedName("location")
    private String location;
    
    @SerializedName("timestamp")
    private long timestamp;
    
    @SerializedName("is_active")
    private boolean isActive;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("source")
    private String source;
    
    @SerializedName("expires_at")
    private long expiresAt;

    public DisasterAlert() {}

    public DisasterAlert(String id, String title, String description, String severity, String location, long timestamp, boolean isActive) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.location = location;
        this.timestamp = timestamp;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
