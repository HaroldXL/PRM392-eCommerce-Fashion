package com.example.prm392_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.activities.AdminDashboardActivity;
import com.example.prm392_finalproject.activities.ForgotPasswordActivity;
import com.example.prm392_finalproject.models.AuthResponse;
import com.example.prm392_finalproject.models.LoginRequest;
import com.example.prm392_finalproject.models.User;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        initViews();

        // Initialize API service and session manager
        apiService = RetrofitClient.createService(ApiService.class);
        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        // Set click listeners
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        // Reset errors
        etEmail.setError(null);
        etPassword.setError(null);

        // Get input values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Show progress
        setLoading(true);

        // Create login request
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Make API call
        Call<AuthResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    if (authResponse.isSuccess()) {
                        // Save session
                        String token = authResponse.getToken() != null ? authResponse.getToken() : "";
                        String userName = authResponse.getEmail() != null ? authResponse.getEmail() : email;
                        int role = authResponse.getRole();

                        sessionManager.createLoginSession(token, email, userName);

                        // Fetch user profile to get user ID and then navigate based on role
                        fetchUserProfile(token, role);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.login_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            getString(R.string.login_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        getString(R.string.network_error) + ": " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_email_empty));
            etEmail.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_email_invalid));
            etEmail.requestFocus();
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_password_empty));
            if (isValid)
                etPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError(getString(R.string.error_password_short));
            if (isValid)
                etPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.5f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnLogin.setAlpha(1.0f);
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchUserProfile(String token, int role) {
        Call<User> call = apiService.getUserProfile("Bearer " + token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Save user ID to session
                    sessionManager.saveUserId(user.getId());

                    // Show success message
                    Toast.makeText(LoginActivity.this,
                            getString(R.string.login_success),
                            Toast.LENGTH_SHORT).show();

                    // Navigate based on role
                    navigateBasedOnRole(role);
                } else {
                    // Even if profile fetch fails, we can still proceed
                    Toast.makeText(LoginActivity.this,
                            getString(R.string.login_success),
                            Toast.LENGTH_SHORT).show();
                    navigateBasedOnRole(role);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Even if profile fetch fails, we can still proceed
                Toast.makeText(LoginActivity.this,
                        getString(R.string.login_success),
                        Toast.LENGTH_SHORT).show();
                navigateBasedOnRole(role);
            }
        });
    }

    private void navigateBasedOnRole(int role) {
        // Role 2 = Customer -> Main shopping activity
        // Role 1 = Admin, Role 3 = Manager -> Admin dashboard
        if (role == 2) {
            navigateToMain();
        } else {
            navigateToAdminDashboard();
        }
    }
}
