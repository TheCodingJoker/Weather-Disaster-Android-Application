package com.mzansi.solutions.disasterdetectoralert.services;

import com.mzansi.solutions.disasterdetectoralert.models.DisasterAlert;
import com.mzansi.solutions.disasterdetectoralert.models.WeatherData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RealTimeDisasterAlertService {
    
    public static List<DisasterAlert> generateRealTimeAlerts(WeatherData weatherData, String location) {
        List<DisasterAlert> alerts = new ArrayList<>();
        Random random = new Random();
        
        // Generate alerts based on real weather conditions
        double temperature = weatherData.getTemperature();
        double windSpeed = weatherData.getWindSpeed();
        double humidity = weatherData.getHumidity();
        String weatherDescription = weatherData.getWeather().getDescription().toLowerCase();
        
        // Heat wave alerts
        if (temperature > 35) {
            DisasterAlert heatAlert = new DisasterAlert(
                    "heat_" + System.currentTimeMillis(),
                    "Heat Wave Warning",
                    "Extreme temperatures detected. Heat index may reach dangerous levels. Stay hydrated and avoid outdoor activities.",
                    "HIGH",
                    location,
                    System.currentTimeMillis(),
                    true
            );
            heatAlert.setCategory("Weather");
            heatAlert.setSource("SA Weather Service");
            heatAlert.setExpiresAt(System.currentTimeMillis() + 86400000); // 24 hours
            alerts.add(heatAlert);
        } else if (temperature > 30) {
            DisasterAlert heatAdvisory = new DisasterAlert(
                    "heat_advisory_" + System.currentTimeMillis(),
                    "Heat Advisory",
                    "High temperatures expected. Drink plenty of water and limit outdoor activities during peak hours.",
                    "MEDIUM",
                    location,
                    System.currentTimeMillis(),
                    true
            );
            heatAdvisory.setCategory("Weather");
            heatAdvisory.setSource("SA Weather Service");
            heatAdvisory.setExpiresAt(System.currentTimeMillis() + 43200000); // 12 hours
            alerts.add(heatAdvisory);
        }
        
        // Wind alerts
        if (windSpeed > 50) {
            DisasterAlert windWarning = new DisasterAlert(
                    "wind_warning_" + System.currentTimeMillis(),
                    "High Wind Warning",
                    "Strong winds detected with speeds up to " + (int)windSpeed + " km/h. Secure loose objects and avoid outdoor activities.",
                    "HIGH",
                    location,
                    System.currentTimeMillis(),
                    true
            );
            windWarning.setCategory("Weather");
            windWarning.setSource("SA Weather Service");
            windWarning.setExpiresAt(System.currentTimeMillis() + 21600000); // 6 hours
            alerts.add(windWarning);
        } else if (windSpeed > 30) {
            DisasterAlert windAdvisory = new DisasterAlert(
                    "wind_advisory_" + System.currentTimeMillis(),
                    "Wind Advisory",
                    "Moderate to strong winds expected. Be cautious when driving and secure outdoor furniture.",
                    "MEDIUM",
                    location,
                    System.currentTimeMillis(),
                    true
            );
            windAdvisory.setCategory("Weather");
            windAdvisory.setSource("SA Weather Service");
            windAdvisory.setExpiresAt(System.currentTimeMillis() + 10800000); // 3 hours
            alerts.add(windAdvisory);
        }
        
        // Rain and storm alerts
        if (weatherDescription.contains("thunderstorm") || weatherDescription.contains("storm")) {
            DisasterAlert stormWarning = new DisasterAlert(
                    "storm_warning_" + System.currentTimeMillis(),
                    "Thunderstorm Warning",
                    "Severe thunderstorms detected in your area. Seek shelter immediately and avoid open areas.",
                    "HIGH",
                    location,
                    System.currentTimeMillis(),
                    true
            );
            stormWarning.setCategory("Weather");
            stormWarning.setSource("SA Weather Service");
            stormWarning.setExpiresAt(System.currentTimeMillis() + 7200000); // 2 hours
            alerts.add(stormWarning);
        } else if (weatherDescription.contains("rain") && humidity > 80) {
            DisasterAlert rainAdvisory = new DisasterAlert(
                    "rain_advisory_" + System.currentTimeMillis(),
                    "Heavy Rain Advisory",
                    "Heavy rainfall expected. Be cautious of flash flooding and slippery roads.",
                    "MEDIUM",
                    location,
                    System.currentTimeMillis(),
                    true
            );
            rainAdvisory.setCategory("Weather");
            rainAdvisory.setSource("SA Weather Service");
            rainAdvisory.setExpiresAt(System.currentTimeMillis() + 14400000); // 4 hours
            alerts.add(rainAdvisory);
        }
        
        // Cold weather alerts
        if (temperature < 5) {
            DisasterAlert coldWarning = new DisasterAlert(
                    "cold_warning_" + System.currentTimeMillis(),
                    "Cold Weather Warning",
                    "Extremely cold temperatures detected. Dress warmly and be aware of frostbite risk.",
                    "HIGH",
                    location,
                    System.currentTimeMillis(),
                    true
            );
            coldWarning.setCategory("Weather");
            coldWarning.setSource("SA Weather Service");
            coldWarning.setExpiresAt(System.currentTimeMillis() + 86400000); // 24 hours
            alerts.add(coldWarning);
        } else if (temperature < 10) {
            DisasterAlert coldAdvisory = new DisasterAlert(
                    "cold_advisory_" + System.currentTimeMillis(),
                    "Cold Weather Advisory",
                    "Cold temperatures expected. Dress in layers and protect exposed skin.",
                    "MEDIUM",
                    location,
                    System.currentTimeMillis(),
                    true
            );
            coldAdvisory.setCategory("Weather");
            coldAdvisory.setSource("SA Weather Service");
            coldAdvisory.setExpiresAt(System.currentTimeMillis() + 43200000); // 12 hours
            alerts.add(coldAdvisory);
        }
        
        // Fire risk alerts (based on temperature and humidity)
        if (temperature > 25 && humidity < 30 && windSpeed > 15) {
            DisasterAlert fireRisk = new DisasterAlert(
                    "fire_risk_" + System.currentTimeMillis(),
                    "High Fire Risk Warning",
                    "High fire risk conditions detected. Avoid outdoor fires and be extremely cautious with flammable materials.",
                    "HIGH",
                    location,
                    System.currentTimeMillis(),
                    true
            );
            fireRisk.setCategory("Fire");
            fireRisk.setSource("SA Fire Service");
            fireRisk.setExpiresAt(System.currentTimeMillis() + 21600000); // 6 hours
            alerts.add(fireRisk);
        }
        
        // UV index alerts (if available)
        if (weatherData.getUvIndex() > 8) {
            DisasterAlert uvWarning = new DisasterAlert(
                    "uv_warning_" + System.currentTimeMillis(),
                    "High UV Index Warning",
                    "Extremely high UV index detected. Avoid sun exposure between 10 AM and 4 PM. Use sunscreen and protective clothing.",
                    "MEDIUM",
                    location,
                    System.currentTimeMillis(),
                    true
            );
            uvWarning.setCategory("Health");
            uvWarning.setSource("SA Health Department");
            uvWarning.setExpiresAt(System.currentTimeMillis() + 28800000); // 8 hours
            alerts.add(uvWarning);
        }
        
        return alerts;
    }
    
    public static List<DisasterAlert> generateDetailedAlerts(WeatherData weatherData, String location) {
        List<DisasterAlert> alerts = generateRealTimeAlerts(weatherData, location);
        
        // Add more detailed information to existing alerts
        for (DisasterAlert alert : alerts) {
            // Add detailed instructions based on alert type
            if (alert.getTitle().contains("Heat")) {
                alert.setDescription(alert.getDescription() + 
                    "\n\nDetailed Instructions:\n" +
                    "1. Stay indoors in air-conditioned spaces\n" +
                    "2. Drink plenty of water (at least 8 glasses per day)\n" +
                    "3. Avoid alcoholic and caffeinated beverages\n" +
                    "4. Wear loose, light-colored clothing\n" +
                    "5. Check on elderly neighbors and family members\n" +
                    "6. Never leave children or pets in vehicles");
            } else if (alert.getTitle().contains("Wind")) {
                alert.setDescription(alert.getDescription() + 
                    "\n\nDetailed Instructions:\n" +
                    "1. Secure outdoor furniture and loose objects\n" +
                    "2. Avoid driving unless absolutely necessary\n" +
                    "3. Stay away from trees and power lines\n" +
                    "4. Keep emergency supplies ready\n" +
                    "5. Close and secure all windows and doors\n" +
                    "6. Monitor local news for updates");
            } else if (alert.getTitle().contains("Storm")) {
                alert.setDescription(alert.getDescription() + 
                    "\n\nDetailed Instructions:\n" +
                    "1. Seek shelter immediately in a sturdy building\n" +
                    "2. Avoid open areas, tall trees, and metal objects\n" +
                    "3. Stay away from windows and doors\n" +
                    "4. Unplug electrical appliances\n" +
                    "5. Have a battery-powered radio ready\n" +
                    "6. Wait 30 minutes after the last thunder before going outside");
            }
        }
        
        return alerts;
    }
}
