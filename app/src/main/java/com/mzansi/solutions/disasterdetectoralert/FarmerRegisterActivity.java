package com.mzansi.solutions.disasterdetectoralert;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FarmerRegisterActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;

    // Views
    private ImageButton btnBack;
    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialCheckBox cbTerms;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin;
    
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize timeout handler
        timeoutHandler = new Handler();
        
        // Log Firebase initialization
        android.util.Log.d("FarmerRegisterActivity", "Firebase Auth initialized: " + (mAuth != null));

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Register button
        btnRegister.setOnClickListener(v -> handleRegistration());

        // Login link
        tvLogin.setOnClickListener(v -> {
            // Go back to login
            finish();
        });
    }

    private void handleRegistration() {
        // Get input values
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Reset errors
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Validate inputs
        boolean isValid = true;

        // Full name validation
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full name is required");
            isValid = false;
        } else if (fullName.length() < 2) {
            tilFullName.setError("Please enter your full name");
            isValid = false;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else if (!isPasswordStrong(password)) {
            tilPassword.setError("Password must contain letters and numbers");
            isValid = false;
        }

        // Confirm password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        // Terms acceptance validation
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!isValid) {
            return;
        }
        
        // Prevent double-click
        if (isRegistering) {
            android.util.Log.d("FarmerRegisterActivity", "Already registering, ignoring click");
            return;
        }
        isRegistering = true;

        // Show loading
        showLoading(true);

        android.util.Log.d("FarmerRegisterActivity", "Registration attempt - Name: " + fullName + ", Email: " + email);
        
        // Set timeout (15 seconds)
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRegistering) {
                    android.util.Log.e("FarmerRegisterActivity", "Registration timeout! Firebase not responding.");
                    isRegistering = false;
                    showLoading(false);
                    Toast.makeText(FarmerRegisterActivity.this, 
                        "Registration timeout. Please check:\n" +
                        "1. Internet connection\n" +
                        "2. Firebase Authentication is enabled", 
                        Toast.LENGTH_LONG).show();
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 15000); // 15 second timeout

        // Create user with Firebase Authentication
        final String finalFullName = fullName;
        
        android.util.Log.d("FarmerRegisterActivity", "Calling Firebase createUserWithEmailAndPassword...");
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Cancel timeout
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        isRegistering = false;
                        
                        android.util.Log.d("FarmerRegisterActivity", "Firebase Auth response received");
                        
                        if (task.isSuccessful()) {
                            // Registration successful
                            android.util.Log.d("FarmerRegisterActivity", "createUserWithEmail:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            
                            if (firebaseUser != null) {
                                android.util.Log.d("FarmerRegisterActivity", "Firebase user created: " + firebaseUser.getUid());
                                
                                // Update user profile with display name
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(finalFullName)
                                        .build();
                                
                                firebaseUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {
                                            showLoading(false);
                                            
                                            if (profileTask.isSuccessful()) {
                                                android.util.Log.d("FarmerRegisterActivity", "User display name set");
                                            }
                                            
                                            Toast.makeText(FarmerRegisterActivity.this, 
                                                    "Registration successful! Please login.", 
                                                    Toast.LENGTH_LONG).show();
                                            
                                            // Navigate back to login
                                            finish();
                                        });
                            } else {
                                // This shouldn't happen
                                showLoading(false);
                                android.util.Log.e("FarmerRegisterActivity", "Firebase user is null after successful registration!");
                                Toast.makeText(FarmerRegisterActivity.this, 
                                    "Registration error: User is null", 
                                    Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // Registration failed
                            showLoading(false);
                            android.util.Log.w("FarmerRegisterActivity", "createUserWithEmail:failure", task.getException());
                            
                            String errorMessage = "Registration failed. ";
                            if (task.getException() != null) {
                                String exceptionMessage = task.getException().getMessage();
                                android.util.Log.e("FarmerRegisterActivity", "Error details: " + exceptionMessage);
                                
                                if (exceptionMessage != null) {
                                    if (exceptionMessage.contains("email address is already in use")) {
                                        errorMessage += "This email is already registered.";
                                    } else if (exceptionMessage.contains("network error")) {
                                        errorMessage += "Please check your internet connection.";
                                    } else if (exceptionMessage.contains("CONFIGURATION_NOT_FOUND")) {
                                        errorMessage += "Firebase not configured. Please add google-services.json";
                                    } else if (exceptionMessage.contains("API key")) {
                                        errorMessage += "Invalid API key in google-services.json";
                                    } else {
                                        errorMessage += exceptionMessage;
                                    }
                                }
                            } else {
                                errorMessage += "Unknown error occurred.";
                            }
                            
                            Toast.makeText(FarmerRegisterActivity.this, errorMessage, 
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean isPasswordStrong(String password) {
        // Check if password contains both letters and numbers
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }

            if (hasLetter && hasDigit) {
                return true;
            }
        }

        return false;
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.GONE);
            btnRegister.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up timeout handler
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }
}
