package com.mzansi.solutions.disasterdetectoralert.models;

public class DetailedAlert {
    private String id;
    private String title;
    private String description;
    private String severity;
    private String location;
    private String affectedArea;
    private String issuedBy;
    private String instructions;
    private String duration;
    private long startTime;
    private long endTime;
    private long timestamp;
    private boolean isActive;
    private String category; // e.g., "Weather", "Fire", "Flood", "Earthquake"

    public DetailedAlert() {}

    public DetailedAlert(String id, String title, String description, String severity, 
                        String location, String affectedArea, String issuedBy, 
                        String instructions, String duration, long startTime, 
                        long endTime, long timestamp, boolean isActive, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.location = location;
        this.affectedArea = affectedArea;
        this.issuedBy = issuedBy;
        this.instructions = instructions;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timestamp = timestamp;
        this.isActive = isActive;
        this.category = category;
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

    public String getAffectedArea() {
        return affectedArea;
    }

    public void setAffectedArea(String affectedArea) {
        this.affectedArea = affectedArea;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
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
}




