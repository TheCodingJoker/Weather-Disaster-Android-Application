import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

// Load API keys from local.properties
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
}

// Get API keys with fallback values for template
fun getLocalProperty(key: String, defaultValue: String = ""): String {
    return localProperties.getProperty(key) ?: defaultValue
}

val geminiApiKey = getLocalProperty("GEMINI_API_KEY", "YOUR_GEMINI_API_KEY_HERE")
val googlePlacesApiKey = getLocalProperty("GOOGLE_PLACES_API_KEY", "YOUR_GOOGLE_PLACES_API_KEY_HERE")
val weatherbitApiKey = getLocalProperty("WEATHERBIT_API_KEY", "YOUR_WEATHERBIT_API_KEY_HERE")

android {
    namespace = "com.mzansi.solutions.disasterdetectoralert"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mzansi.solutions.disasterdetectoralert"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Add API keys to BuildConfig
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "GOOGLE_PLACES_API_KEY", "\"$googlePlacesApiKey\"")
        buildConfigField("String", "WEATHERBIT_API_KEY", "\"$weatherbitApiKey\"")
        
        // Add Google Places API key to manifest
        manifestPlaceholders["GOOGLE_PLACES_API_KEY"] = googlePlacesApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    
    // Google Places API
    implementation("com.google.android.libraries.places:places:3.3.0")
    
    // Location services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-contextmanager:9.4.0")
    
    // Permissions
    implementation("androidx.fragment:fragment:1.6.2")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    
    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // Charts for historical data
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Firebase - BOM manages all versions automatically
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
