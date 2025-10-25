# Google Places API Setup

## Required Steps

### 1. Get Google Places API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Places API** and **Maps SDK for Android**
4. Create credentials (API Key)
5. Restrict the API key to your app's package name and SHA-1 fingerprint

### 2. Update API Key in AndroidManifest.xml
Replace `YOUR_GOOGLE_PLACES_API_KEY` in `app/src/main/AndroidManifest.xml` with your actual API key:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_ACTUAL_API_KEY_HERE" />
```

### 3. Update API Key in LocationActivity.java
Replace `YOUR_GOOGLE_PLACES_API_KEY` in `LocationActivity.java` with your actual API key:

```java
Places.initialize(getApplicationContext(), "YOUR_ACTUAL_API_KEY_HERE");
```

## Features Implemented

✅ **Location Search**: Users can search for locations using Google Places Autocomplete
✅ **Current Location**: Users can use their device's GPS location
✅ **Location Storage**: Selected location is saved in SharedPreferences
✅ **Permission Handling**: Proper location permission requests
✅ **Responsive UI**: Modern Material Design interface
✅ **Navigation**: Seamless flow from role selection to location selection

## Usage

1. User selects "Community Member" on the splash screen
2. LocationActivity opens with two options:
   - **Search for location**: Opens Google Places Autocomplete
   - **Use Current Location**: Uses device GPS (requires permission)
3. Selected location is displayed and can be confirmed
4. Location is saved to SharedPreferences for future use



