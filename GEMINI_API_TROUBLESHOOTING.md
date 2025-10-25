# Gemini API 404 Error - Troubleshooting Guide

## Available Model Names to Try

If you're getting a 404 error, try these endpoints in order:

### Option 1: gemini-1.5-pro (Current)
```
https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent
```

### Option 2: gemini-1.0-pro
```
https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent
```

### Option 3: gemini-pro (Legacy)
```
https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
```

### Option 4: gemini-1.5-flash
```
https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
```

## How to Change the Model

Edit `GeminiApiService.java` line 21 and change the URL.

## Verify Available Models

You can check which models are available with your API key:

```bash
curl "https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_API_KEY"
```

This will show you all available models for your API key.

## Enable the API

Make sure the Generative Language API is enabled:
1. Go to https://console.cloud.google.com/
2. Select your project
3. Go to "APIs & Services" > "Library"
4. Search for "Generative Language API"
5. Click "Enable"

## Check API Key Permissions

1. Go to https://aistudio.google.com/app/apikey
2. Make sure your API key has the correct permissions
3. Try creating a new API key if the current one doesn't work

