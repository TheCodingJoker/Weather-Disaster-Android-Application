package com.mzansi.solutions.disasterdetectoralert.models;

public class FarmingTip {
    private String title;
    private String description;
    private String category; // planting, irrigation, pest_control, harvest, general
    private int priority; // 1-5, 5 being highest
    private String icon;
    
    public FarmingTip(String title, String description, String category, int priority) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.icon = getCategoryIcon(category);
    }
    
    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public int getPriority() { return priority; }
    public String getIcon() { return icon; }
    
    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { 
        this.category = category;
        this.icon = getCategoryIcon(category);
    }
    public void setPriority(int priority) { this.priority = priority; }
    
    private String getCategoryIcon(String category) {
        switch (category) {
            case "planting": return "🌱";
            case "irrigation": return "💧";
            case "pest_control": return "🐛";
            case "harvest": return "🌾";
            case "weather": return "🌤️";
            default: return "ℹ️";
        }
    }
}




