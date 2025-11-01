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

import com.example.prm392_finalproject.models.AuthResponse;
import com.example.prm392_finalproject.models.RegisterRequest;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvSignIn;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        initViews();

        // Initialize API service and session manager
        apiService = RetrofitClient.createService(ApiService.class);
        sessionManager = new SessionManager(this);

        // Set click listeners
        setupClickListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvSignIn = findViewById(R.id.tvSignIn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        tvSignIn.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private void attemptRegister() {
        // Reset errors
        clearErrors();

        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(fullName, email, phone, address, password, confirmPassword)) {
            return;
        }

        // Show progress
        setLoading(true);

        // Create register request
        RegisterRequest registerRequest = new RegisterRequest(email, password, fullName, phone, address);

        // Make API call
        Call<AuthResponse> call = apiService.register(registerRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    if (authResponse.isSuccess()) {
                        // Show success message
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.register_success),
                                Toast.LENGTH_SHORT).show();

                        // Optionally auto-login after registration
                        String token = authResponse.getToken() != null ? authResponse.getToken() : "";
                        sessionManager.createLoginSession(token, email, fullName);

                        // Navigate to main activity
                        navigateToMain();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.register_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this,
                            getString(R.string.register_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        getString(R.string.network_error) + ": " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String fullName, String email, String phone,
            String address, String password, String confirmPassword) {
        boolean isValid = true;

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError(getString(R.string.error_full_name_empty));
            etFullName.requestFocus();
            isValid = false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_email_empty));
            if (isValid)
                etEmail.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_email_invalid));
            if (isValid)
                etEmail.requestFocus();
            isValid = false;
        }

        // Validate phone
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError(getString(R.string.error_phone_empty));
            if (isValid)
                etPhone.requestFocus();
            isValid = false;
        } else if (phone.length() < 10) {
            etPhone.setError(getString(R.string.error_phone_invalid));
            if (isValid)
                etPhone.requestFocus();
            isValid = false;
        }

        // Validate address
        if (TextUtils.isEmpty(address)) {
            etAddress.setError(getString(R.string.error_address_empty));
            if (isValid)
                etAddress.requestFocus();
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

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_password_empty));
            if (isValid)
                etConfirmPassword.requestFocus();
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_password_mismatch));
            if (isValid)
                etConfirmPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        etFullName.setError(null);
        etEmail.setError(null);
        etPhone.setError(null);
        etAddress.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            btnRegister.setAlpha(0.5f);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            btnRegister.setAlpha(1.0f);
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
