package com.mzansi.solutions.disasterdetectoralert;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mzansi.solutions.disasterdetectoralert.utils.SessionManager;

public class CommunityLoginActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;

    // Views
    private ImageButton btnBack;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvForgotPassword;
    private TextView tvCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is already logged in
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            // User is already logged in, redirect to dashboard
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_community_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Login button
        btnLogin.setOnClickListener(v -> handleLogin());

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // Create account
        tvCreateAccount.setOnClickListener(v -> handleCreateAccount());
    }

    private void handleLogin() {
        // Get input values
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Reset errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validate inputs
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Show loading
        showLoading(true);

        android.util.Log.d("CommunityLoginActivity", "Login attempt - Email: " + email);

        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        
                        if (task.isSuccessful()) {
                            // Login successful
                            android.util.Log.d("CommunityLoginActivity", "signInWithEmail:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                String userEmail = firebaseUser.getEmail();
                                String userName = firebaseUser.getDisplayName();
                                
                                // Use display name if available, otherwise use "Community Member"
                                if (userName == null || userName.isEmpty()) {
                                    userName = "Community Member";
                                }
                                
                                android.util.Log.d("CommunityLoginActivity", "User logged in: " + userId);
                                
                                // Create login session with community type
                                sessionManager.createLoginSession(userId, userEmail, userName, SessionManager.USER_TYPE_COMMUNITY);
                                
                                Toast.makeText(CommunityLoginActivity.this, 
                                        "Welcome back, " + userName + "!", 
                                        Toast.LENGTH_SHORT).show();
                                
                                // Navigate to dashboard
                                navigateToDashboard();
                            }
                        } else {
                            // Login failed
                            android.util.Log.w("CommunityLoginActivity", "signInWithEmail:failure", task.getException());
                            
                            String errorMessage = "Login failed. ";
                            if (task.getException() != null) {
                                String exceptionMessage = task.getException().getMessage();
                                if (exceptionMessage != null) {
                                    if (exceptionMessage.contains("no user record") || 
                                        exceptionMessage.contains("user not found")) {
                                        errorMessage += "No account found with this email.";
                                    } else if (exceptionMessage.contains("password is invalid") || 
                                               exceptionMessage.contains("wrong password")) {
                                        errorMessage += "Incorrect password.";
                                    } else if (exceptionMessage.contains("network error")) {
                                        errorMessage += "Please check your internet connection.";
                                    } else {
                                        errorMessage += exceptionMessage;
                                    }
                                }
                            }
                            
                            tilPassword.setError("Invalid credentials");
                            Toast.makeText(CommunityLoginActivity.this, errorMessage, 
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void navigateToDashboard() {
        // Navigate to location selection
        // After location is selected, user proceeds to DashboardActivity
        Intent intent = new Intent(this, LocationActivity.class);
        intent.putExtra("is_farmer", false);
        startActivity(intent);
        finish();
    }

    private void handleForgotPassword() {
        android.util.Log.d("CommunityLoginActivity", "Forgot password clicked");
        
        // Get email from the field if available
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Please enter your email first");
            etEmail.requestFocus();
            return;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }
        
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        
        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (task.isSuccessful()) {
                        android.util.Log.d("CommunityLoginActivity", "Password reset email sent");
                        Toast.makeText(CommunityLoginActivity.this, 
                                "Password reset link sent to " + email, 
                                Toast.LENGTH_LONG).show();
                    } else {
                        android.util.Log.w("CommunityLoginActivity", "Error sending password reset email", task.getException());
                        
                        String errorMessage = "Failed to send reset email. ";
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage != null && exceptionMessage.contains("no user record")) {
                                errorMessage += "No account found with this email.";
                            } else {
                                errorMessage += "Please try again.";
                            }
                        }
                        
                        Toast.makeText(CommunityLoginActivity.this, errorMessage, 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleCreateAccount() {
        android.util.Log.d("CommunityLoginActivity", "Create account clicked");
        
        // Navigate to registration activity
        Intent intent = new Intent(this, CommunityRegisterActivity.class);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
            btnLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

