# Disaster Detector Alert 🌍⚠️

A comprehensive Android application that provides real-time disaster alerts, weather forecasting, and AI-powered farming assistance for South African communities.

## 📱 Features

### For Community Members
- **🌍 Real-time Disaster Alerts**: Get instant notifications about disasters in your area
- **🌤️ Weather Dashboard**: Current weather conditions with detailed metrics
- **📅 16-Day Forecast**: Extended weather predictions
- **👕 AI Clothing Suggestions**: Smart outfit recommendations based on weather using Gemini AI
- **📍 Location Services**: Search for locations or use your current location

### For Farmers
- **🌾 Farmer Dashboard**: Specialized interface for agricultural needs
- **🤖 AI Farming Tips**: Personalized farming advice powered by Google Gemini AI
- **📊 Weather Analytics**: Visual charts for temperature, humidity, and precipitation trends
- **🌱 Crop Management**: Track and manage your active crops
- **📋 Upcoming Tasks**: AI-generated task lists based on weather and crops
- **☔ Planting Conditions**: Real-time assessment of planting suitability

## 🎨 Design

- **Modern Material Design 3** with consistent color scheme
- **Intuitive Navigation** with bottom navigation bar
- **Responsive Layouts** that adapt to different screen sizes
- **Beautiful UI** with smooth transitions and animations

## 🔧 Technologies Used

### Core
- **Language**: Java
- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 15 (API 35)

### APIs & Services
- **Google Gemini AI**: AI-powered tips and suggestions
- **Google Places API**: Location search and autocomplete
- **Weatherbit API**: Weather data and forecasts
- **Retrofit**: REST API communication
- **OkHttp**: HTTP client with logging

### Libraries
- **Material Components**: Modern UI components
- **Glide**: Image loading and caching
- **MPAndroidChart**: Beautiful charts for data visualization
- **Gson**: JSON parsing
- **Google Play Services**: Location and Maps

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- JDK 8 or higher
- Android SDK with API 35

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/DisasterDetectorAlert.git
   cd DisasterDetectorAlert
   ```

2. **Set up API keys**
   
   Copy the template file:
   ```bash
   cp local.properties.template local.properties
   ```
   
   Then edit `local.properties` and add your API keys:
   ```properties
   sdk.dir=YOUR_SDK_PATH
   
   GEMINI_API_KEY=your_gemini_api_key_here
   GOOGLE_PLACES_API_KEY=your_google_places_key_here
   WEATHERBIT_API_KEY=your_weatherbit_key_here
   ```

3. **Get API Keys**
   
   See the detailed [API Keys Setup Guide](API_KEYS_SETUP.md) for instructions on obtaining:
   - [Gemini API Key](https://makersuite.google.com/app/apikey)
   - [Google Places API Key](https://console.cloud.google.com/)
   - [Weatherbit API Key](https://www.weatherbit.io/account/create)

4. **Build and Run**
   ```bash
   ./gradlew clean build
   ```
   
   Or open the project in Android Studio and click Run ▶️

## 📖 User Guide

### Community Member Flow
1. Launch the app
2. Select "Community Member"
3. Choose or search for your location
4. View weather and disaster alerts on the dashboard
5. Navigate between Dashboard, Alerts, Forecast, and Settings

### Farmer Flow
1. Launch the app
2. Select "Farmer"
3. Create an account or login
4. Set your farm location
5. Add your crops in Crop Management
6. View AI-powered farming tips and weather analytics
7. Check upcoming tasks based on weather predictions

## 🗂️ Project Structure

```
app/src/main/java/com/mzansi/solutions/disasterdetectoralert/
├── activities/
│   ├── MainActivity.java           # Splash screen
│   ├── LocationActivity.java       # Location selection
│   ├── DashboardActivity.java      # Community dashboard
│   ├── FarmerDashboardActivity.java # Farmer dashboard
│   ├── ForecastActivity.java       # Weather forecast
│   ├── AlertsActivity.java         # Disaster alerts
│   └── SettingsActivity.java       # App settings
├── adapters/                        # RecyclerView adapters
├── api/
│   ├── GeminiApiService.java       # Gemini AI integration
│   ├── WeatherApiService.java      # Weather API interface
│   └── ApiClient.java              # Retrofit client
├── models/                          # Data models
├── services/                        # Background services
└── utils/                           # Utility classes
```

## 🔐 Security

- ✅ All API keys are stored in `local.properties` (gitignored)
- ✅ Keys are accessed via `BuildConfig` at runtime
- ✅ No sensitive data committed to version control
- ✅ Template files provided for easy setup

See [API_KEYS_SETUP.md](API_KEYS_SETUP.md) for security best practices.

## 🌟 Key Features Explained

### AI-Powered Features (Gemini)
- **Safety Tips**: Context-aware disaster safety advice
- **Clothing Suggestions**: Weather-appropriate outfit recommendations
- **Farming Tips**: Crop-specific advice based on weather conditions
- **Task Planning**: Automated farming task scheduling

### Weather Intelligence
- **Current Conditions**: Real-time weather data
- **16-Day Forecast**: Extended predictions with daily details
- **Historical Trends**: Visual charts showing weather patterns
- **Disaster Correlation**: Weather-based disaster risk assessment

### Farmer-Specific Tools
- **Crop Manager**: Track planting dates and crop status
- **Planting Advisor**: AI-powered planting condition assessment
- **Weekly Rainfall**: Cumulative precipitation tracking
- **Task Automation**: Smart task generation based on weather and crops

## 📊 API Usage

### Free Tier Limits
- **Gemini**: 60 requests/minute
- **Google Places**: $200/month credit (~70 autocomplete/day)
- **Weatherbit**: 500 calls/day

See [API_KEYS_SETUP.md](API_KEYS_SETUP.md) for detailed pricing and recommendations.

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 Documentation

- [API Keys Setup Guide](API_KEYS_SETUP.md) - Detailed API configuration
- [GitHub Upload Guide](GITHUB_UPLOAD_GUIDE.md) - How to safely upload to GitHub
- [Forecast Feature Guide](FORECAST_FEATURE_GUIDE.md) - Forecast functionality
- [Settings Feature Guide](SETTINGS_FEATURE_GUIDE.md) - Settings configuration
- [Gemini AI Integration](GEMINI_AI_INTEGRATION_COMPLETE.md) - AI features

## 🐛 Troubleshooting

### Build Issues
- Ensure all API keys are set in `local.properties`
- Run `./gradlew clean build`
- Check that SDK path is correct

### Location Not Working
- Enable location permissions in device settings
- Check Google Places API key restrictions
- Verify SHA-1 fingerprint is added to Google Cloud Console

### Weather Data Not Loading
- Verify Weatherbit API key is valid
- Check API call limit (500/day on free tier)
- Ensure internet connection is active

## 📜 License

This project is intended for educational purposes. Please ensure you comply with all API provider terms of service.

## 👨‍💻 Developer

Developed by Mzansi Solutions

## 🙏 Acknowledgments

- **Google Gemini AI** for intelligent content generation
- **Weatherbit.io** for comprehensive weather data
- **Google Places API** for location services
- **MPAndroidChart** for beautiful data visualization
- **Material Design** for UI components

---

**Note**: This application requires active API keys to function. See [API_KEYS_SETUP.md](API_KEYS_SETUP.md) for setup instructions.

**Last Updated**: October 2025

