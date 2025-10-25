package com.mzansi.solutions.disasterdetectoralert.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApiService {
    // Using gemini-2.5-flash - stable model (June 2025)
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final String API_KEY = com.mzansi.solutions.disasterdetectoralert.BuildConfig.GEMINI_API_KEY; // Get free key from https://makersuite.google.com/app/apikey
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private OkHttpClient client;
    private Gson gson;

    public GeminiApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }

    public interface SafetyTipsCallback {
        void onSuccess(String tips);
        void onError(String error);
    }

    public interface ClothingSuggestionCallback {
        void onSuccess(String suggestion);
        void onError(String error);
    }
    
    public interface FarmingTipsCallback {
        void onSuccess(String tips);
        void onError(String error);
    }
    
    public interface UpcomingTasksCallback {
        void onSuccess(String tasks);
        void onError(String error);
    }

    public void generateSafetyTips(String disasterType, String severity, String location, SafetyTipsCallback callback) {
        String prompt = createSafetyTipsPrompt(disasterType, severity, location);
        makeGeminiRequest(prompt, new GeminiCallback() {
            @Override
            public void onSuccess(String content) {
                callback.onSuccess(content);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void generateClothingSuggestion(String weatherCondition, String temperature, 
                                          double precipitationProbability, double windSpeed, 
                                          String location, ClothingSuggestionCallback callback) {
        String prompt = createClothingSuggestionPrompt(weatherCondition, temperature, 
                precipitationProbability, windSpeed, location);
        makeGeminiRequest(prompt, new GeminiCallback() {
            @Override
            public void onSuccess(String content) {
                callback.onSuccess(content);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    public void generateFarmingTips(String location, double temperature, double humidity, 
                                   double precipitation, double windSpeed, double weeklyRain,
                                   String activeCrops, FarmingTipsCallback callback) {
        String prompt = createFarmingTipsPrompt(location, temperature, humidity, precipitation, 
                                               windSpeed, weeklyRain, activeCrops);
        makeGeminiRequest(prompt, new GeminiCallback() {
            @Override
            public void onSuccess(String content) {
                callback.onSuccess(content);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    public void generateUpcomingTasks(String location, List<String> cropsList, 
                                     double weeklyRain, int rainyDays, 
                                     boolean goodPlantingWeek, UpcomingTasksCallback callback) {
        String prompt = createUpcomingTasksPrompt(location, cropsList, weeklyRain, rainyDays, goodPlantingWeek);
        makeGeminiRequest(prompt, new GeminiCallback() {
            @Override
            public void onSuccess(String content) {
                callback.onSuccess(content);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private interface GeminiCallback {
        void onSuccess(String content);
        void onError(String error);
    }

    private void makeGeminiRequest(String prompt, GeminiCallback callback) {
        // Build Gemini API request body
        JsonObject requestBody = new JsonObject();
        
        // Create contents array
        com.google.gson.JsonArray contentsArray = new com.google.gson.JsonArray();
        JsonObject content = new JsonObject();
        
        // Create parts array
        com.google.gson.JsonArray partsArray = new com.google.gson.JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        partsArray.add(part);
        
        content.add("parts", partsArray);
        contentsArray.add(content);
        requestBody.add("contents", contentsArray);

        RequestBody body = RequestBody.create(JSON, requestBody.toString());
        
        // Build request with API key as query parameter
        String urlWithKey = GEMINI_API_URL + "?key=" + API_KEY;
        
        Request request = new Request.Builder()
                .url(urlWithKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        android.util.Log.d("Gemini", "Making request to Gemini API");
        android.util.Log.d("Gemini", "Full URL: " + GEMINI_API_URL);
        android.util.Log.d("Gemini", "API Key (first 20 chars): " + (API_KEY != null && API_KEY.length() > 20 ? API_KEY.substring(0, 20) + "..." : "KEY IS NULL OR TOO SHORT"));
        android.util.Log.d("Gemini", "Prompt length: " + prompt.length() + " characters");
        android.util.Log.d("Gemini", "Request body: " + requestBody.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("Gemini", "Network error: " + e.getMessage());
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                
                if (response.isSuccessful()) {
                    android.util.Log.d("Gemini", "Response received successfully");
                    try {
                        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                        
                        // Parse Gemini response structure
                        String content = jsonResponse.getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();
                        
                        android.util.Log.d("Gemini", "Content extracted successfully, length: " + content.length());
                        callback.onSuccess(content);
                    } catch (Exception e) {
                        android.util.Log.e("Gemini", "Parsing error: " + e.getMessage());
                        android.util.Log.e("Gemini", "Response body: " + responseBody);
                        callback.onError("Error parsing response: " + e.getMessage());
                    }
                } else {
                    android.util.Log.e("Gemini", "API error: " + response.code() + " - " + response.message());
                    android.util.Log.e("Gemini", "Error body: " + responseBody);
                    android.util.Log.e("Gemini", "Request URL: " + urlWithKey);
                    
                    String errorMessage = "API error: " + response.code();
                    if (response.code() == 400) {
                        errorMessage = "Invalid request. Check API key and request format.";
                    } else if (response.code() == 403) {
                        errorMessage = "API key invalid or quota exceeded.";
                    } else if (response.code() == 404) {
                        errorMessage = "API endpoint not found. The model or API version may have changed.";
                    } else if (response.code() == 429) {
                        errorMessage = "Rate limit exceeded. Please try again later.";
                    }
                    
                    callback.onError(errorMessage);
                }
                response.close();
            }
        });
    }

    private String createSafetyTipsPrompt(String disasterType, String severity, String location) {
        return String.format(
            "Generate 5 specific safety tips for a %s disaster with %s severity in %s. " +
            "Make the tips practical, actionable, and relevant to the local context. " +
            "Format each tip as a numbered list with clear, concise instructions. " +
            "Focus on immediate actions people should take to protect themselves and their families.",
            disasterType, severity, location
        );
    }

    private String createClothingSuggestionPrompt(String weatherCondition, String temperature, 
                                                 double precipitationProbability, double windSpeed, 
                                                 String location) {
        return String.format(
            "Generate a practical clothing suggestion for %s weather in %s. " +
            "Temperature: %s, Precipitation chance: %.0f%%, Wind speed: %.0f km/h. " +
            "Provide specific clothing recommendations including: " +
            "1. Base layer (shirt/top) " +
            "2. Outer layer (jacket/sweater) " +
            "3. Bottom wear (pants/shorts) " +
            "4. Footwear " +
            "5. Accessories (hat, gloves, umbrella if needed). " +
            "Make it practical and suitable for the weather conditions. " +
            "Keep the response concise and actionable.",
            weatherCondition, location, temperature, precipitationProbability, windSpeed
        );
    }
    
    private String createFarmingTipsPrompt(String location, double temperature, double humidity,
                                          double precipitation, double windSpeed, double weeklyRain,
                                          String activeCrops) {
        return String.format(
            "You are an expert agricultural advisor. Generate 5-8 specific, actionable farming tips for a farmer in %s. " +
            "Current conditions: Temperature %.1f°C, Humidity %.0f%%, Today's rain: %.1fmm, Wind: %.1fm/s, Weekly rain: %.1fmm. " +
            "Active crops: %s. " +
            "Provide practical advice covering: planting conditions, irrigation needs, pest control/spraying, disease risks, " +
            "weather alerts (heat/frost/wind), and harvest timing. " +
            "Format as a clean list with emoji indicators (🌱 planting, 💧 irrigation, 🌡️ temperature, 🌪️ wind, 🦠 disease, 🌾 harvest). " +
            "Each tip should have a title and brief explanation. Be specific to the current weather.",
            location, temperature, humidity, precipitation, windSpeed, weeklyRain, 
            activeCrops.isEmpty() ? "None specified" : activeCrops
        );
    }
    
    private String createUpcomingTasksPrompt(String location, List<String> cropsList, 
                                            double weeklyRain, int rainyDays, boolean goodPlantingWeek) {
        String cropsInfo = cropsList != null && !cropsList.isEmpty() 
            ? String.join(", ", cropsList) 
            : "No active crops";
            
        return String.format(
            "You are an agricultural planning assistant for a farmer in %s. " +
            "Crops: %s. Weather forecast: %.1fmm rain this week, %d rainy days expected. %s " +
            "Generate a prioritized task list for the upcoming week covering: " +
            "1. Crop-specific tasks (fertilization, monitoring, harvest readiness) " +
            "2. Weather-based preparations (irrigation if dry, drainage if wet, indoor tasks if rainy) " +
            "3. Optimal windows for planting, spraying, or field work. " +
            "Format with clear sections using emojis (🌾 Harvest, 💧 Irrigation, 🌱 Planting, 📅 Schedule, ⚠️ Alerts). " +
            "Keep it practical and actionable for the week ahead.",
            location, cropsInfo, weeklyRain, rainyDays,
            goodPlantingWeek ? "Good planting conditions expected." : "Planting conditions may be challenging."
        );
    }
}



