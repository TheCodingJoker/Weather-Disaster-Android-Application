package com.mzansi.solutions.disasterdetectoralert.models;

import com.google.gson.annotations.SerializedName;

public class Weather {
    @SerializedName("icon")
    private String icon;
    
    @SerializedName("code")
    private int code;
    
    @SerializedName("description")
    private String description;
    
    // Weatherbit provides full icon URLs
    public String getIconUrl() {
        return "https://cdn.weatherbit.io/static/img/icons/" + icon + ".png";
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
