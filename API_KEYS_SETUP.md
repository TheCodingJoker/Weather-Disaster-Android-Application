# API Keys Setup Guide

This document explains how to obtain and configure all required API keys for the Disaster Detector Alert application.

## Overview

The application requires the following API keys:
1. **Gemini API Key** - For AI-powered safety tips, clothing suggestions, and farming advice
2. **Google Places API Key** - For location search functionality
3. **Weatherbit API Key** - For weather data and forecasts
4. **Firebase API Key** - For Firebase services (optional, currently disconnected)

## Important Security Notes

⚠️ **NEVER commit API keys to version control!**

- All API keys are stored in `local.properties` (which is gitignored)
- The app reads keys from `BuildConfig` at runtime
- Template files are provided for reference

---

## 1. Gemini API Key Setup

The Gemini API is used for generating AI-powered content like safety tips, clothing suggestions, and farming advice.

### Steps:

1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click **"Create API Key"**
4. Copy the generated API key
5. Add it to your `local.properties` file:
   ```properties
   GEMINI_API_KEY=AIzaSyAbc123YourActualKeyHere
   ```

### Free Tier:
- 60 requests per minute
- Free quota for testing and development

---

## 2. Google Places API Key Setup

The Google Places API is used for location search and autocomplete functionality.

### Steps:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the following APIs:
   - **Places API**
   - **Maps SDK for Android**
4. Go to **Credentials** → **Create Credentials** → **API Key**
5. **Restrict the API key** (Important for security):
   - Click on the created API key
   - Under "Application restrictions", select **Android apps**
   - Add your package name: `com.mzansi.solutions.disasterdetectoralert`
   - Get your SHA-1 certificate fingerprint:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - Add the SHA-1 fingerprint
   - Under "API restrictions", select **Restrict key**
   - Enable: **Places API** and **Maps SDK for Android**
6. Add it to your `local.properties` file:
   ```properties
   GOOGLE_PLACES_API_KEY=AIzaSyAbc123YourActualKeyHere
   ```

### Pricing:
- Places Autocomplete: $2.83 per 1000 requests (monthly free tier applies)
- Check [Google Maps Platform Pricing](https://developers.google.com/maps/billing-and-pricing/pricing)

---

## 3. Weatherbit API Key Setup

The Weatherbit API provides current weather data and forecasts.

### Steps:

1. Go to [Weatherbit.io](https://www.weatherbit.io/account/create)
2. Sign up for a free account
3. Verify your email address
4. Log in and go to your [API Key page](https://www.weatherbit.io/account/dashboard)
5. Copy your API key
6. Add it to your `local.properties` file:
   ```properties
   WEATHERBIT_API_KEY=abc123youractualkey456
   ```

### Free Tier:
- 500 calls per day
- 16-day forecast
- Current weather data

---

## 4. Firebase Setup (Optional)

Firebase is currently disconnected but the configuration file structure is preserved.

### Steps:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing
3. Add an Android app with package name: `com.mzansi.solutions.disasterdetectoralert`
4. Download the `google-services.json` file
5. Place it in the `app/` directory
6. **Note**: This file is gitignored for security

If you don't plan to use Firebase, you can use the template file provided.

---

## Configuration Files

### local.properties

Your `local.properties` file should look like this:

```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk

# API Keys - DO NOT COMMIT THESE TO VERSION CONTROL
GEMINI_API_KEY=AIzaSyAbc123YourGeminiKeyHere
GOOGLE_PLACES_API_KEY=AIzaSyAbc123YourPlacesKeyHere
WEATHERBIT_API_KEY=abc123yourweatherbitkey456
```

### Template Files

- `local.properties.template` - Copy this to `local.properties` and fill in your keys
- `app/google-services.json.template` - Copy this to `app/google-services.json` (if using Firebase)

---

## Verification

After setting up all API keys, verify the configuration:

1. **Clean and rebuild** the project:
   ```bash
   ./gradlew clean build
   ```

2. **Run the app** and check:
   - Location search works (Google Places)
   - Weather data loads (Weatherbit)
   - AI features generate content (Gemini)

3. **Check logs** for any API key errors:
   ```bash
   adb logcat | grep -i "api"
   ```

---

## Troubleshooting

### "API key not found" error
- Ensure `local.properties` exists in the project root
- Check that all keys are properly formatted (no extra spaces)
- Rebuild the project (`./gradlew clean build`)

### Google Places not working
- Verify the API is enabled in Google Cloud Console
- Check SHA-1 fingerprint is correctly added
- Ensure app package name matches: `com.mzansi.solutions.disasterdetectoralert`

### Weatherbit returning 401 or 403
- Verify your API key is valid
- Check you haven't exceeded the free tier limit (500 calls/day)
- Wait a few minutes after creating a new key

### Gemini API errors
- Ensure you have an active Google account
- Check the API key is from [Google AI Studio](https://makersuite.google.com/app/apikey)
- Verify you haven't exceeded rate limits (60 requests/minute)

---

## Security Best Practices

1. ✅ **Never commit** `local.properties` or `google-services.json` to version control
2. ✅ **Restrict API keys** in their respective consoles
3. ✅ **Monitor usage** to prevent unexpected charges
4. ✅ **Rotate keys** periodically for enhanced security
5. ✅ **Use environment-specific keys** (dev vs production)

---

## Cost Considerations

### Free Tier Summary
- **Gemini**: 60 requests/minute (free for development)
- **Google Places**: $200/month free credit (covers ~70 autocomplete requests/day)
- **Weatherbit**: 500 calls/day (free tier)
- **Firebase**: Spark plan (free with limitations)

### Recommended for Production
- Monitor API usage regularly
- Set up billing alerts
- Implement caching to reduce API calls
- Consider paid tiers for better rate limits

---

## Additional Resources

- [Gemini API Documentation](https://ai.google.dev/docs)
- [Google Places API Documentation](https://developers.google.com/maps/documentation/places/web-service)
- [Weatherbit API Documentation](https://www.weatherbit.io/api)
- [Firebase Documentation](https://firebase.google.com/docs)

---

## Support

If you encounter issues with API key setup:
1. Check the troubleshooting section above
2. Review the official documentation links
3. Check logcat output for specific error messages
4. Ensure all APIs are enabled and properly restricted

---

**Last Updated**: October 2025

