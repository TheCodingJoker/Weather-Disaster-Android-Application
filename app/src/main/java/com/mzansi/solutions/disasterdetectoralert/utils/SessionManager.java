package com.mzansi.solutions.disasterdetectoralert.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SessionManager {
    private static final String PREF_NAME = "FarmerAuthSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;
    private FirebaseAuth mAuth;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String userId, String email, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();

        android.util.Log.d("SessionManager", "Login session created for: " + email);
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        // Check both SharedPreferences and Firebase Auth state
        boolean prefLogin = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        boolean firebaseLoggedIn = currentUser != null;
        
        android.util.Log.d("SessionManager", "Login check - Prefs: " + prefLogin + ", Firebase: " + firebaseLoggedIn);
        
        return prefLogin && firebaseLoggedIn;
    }

    /**
     * Get user ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Get user name
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Farmer");
    }

    /**
     * Get current Firebase user
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Logout user
     */
    public void logout() {
        // Sign out from Firebase
        mAuth.signOut();
        
        // Clear session data
        editor.clear();
        editor.apply();

        android.util.Log.d("SessionManager", "User logged out");
    }

    /**
     * Check if user needs to setup profile
     */
    public boolean needsProfileSetup() {
        String name = getUserName();
        return name == null || name.equals("Farmer");
    }
}




