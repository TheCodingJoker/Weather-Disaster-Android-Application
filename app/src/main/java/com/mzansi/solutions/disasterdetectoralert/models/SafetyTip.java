package com.mzansi.solutions.disasterdetectoralert.models;

public class SafetyTip {
    private String id;
    private String title;
    private String description;
    private String category;
    private String severity;
    private long timestamp;
    private boolean isGenerated;

    public SafetyTip() {}

    public SafetyTip(String id, String title, String description, String category, 
                    String severity, long timestamp, boolean isGenerated) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.severity = severity;
        this.timestamp = timestamp;
        this.isGenerated = isGenerated;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public void setGenerated(boolean generated) {
        isGenerated = generated;
    }
}



