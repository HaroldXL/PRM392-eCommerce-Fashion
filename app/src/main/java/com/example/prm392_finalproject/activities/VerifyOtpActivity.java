package com.example.prm392_finalproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.ForgotPasswordRequest;
import com.example.prm392_finalproject.models.MessageResponse;
import com.example.prm392_finalproject.models.VerifyOtpRequest;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private MaterialButton btnVerify;
    private TextView tvResendCode, tvEmailSent;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Get email from intent
        email = getIntent().getStringExtra("email");
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupListeners();
        updateEmailText();
    }

    private void initViews() {
        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        tvResendCode = findViewById(R.id.tvResendCode);
        tvEmailSent = findViewById(R.id.tvEmailSent);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnVerify.setOnClickListener(v -> verifyOtp());
        tvResendCode.setOnClickListener(v -> resendCode());
    }

    private void updateEmailText() {
        tvEmailSent.setText("We've sent a verification code to " + email);
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            etOtp.setError("OTP is required");
            etOtp.requestFocus();
            return;
        }

        if (otp.length() != 6) {
            etOtp.setError("OTP must be 6 digits");
            etOtp.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);

        apiService.verifyOtp(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnVerify.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    MessageResponse messageResponse = response.body();
                    if (messageResponse.getVerified() != null && messageResponse.getVerified()) {
                        Toast.makeText(VerifyOtpActivity.this,
                                messageResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        // Navigate to Reset Password screen
                        Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(VerifyOtpActivity.this,
                                "Invalid OTP. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VerifyOtpActivity.this,
                            "Invalid OTP. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnVerify.setEnabled(true);
                Toast.makeText(VerifyOtpActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendCode() {
        progressBar.setVisibility(View.VISIBLE);
        tvResendCode.setEnabled(false);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        apiService.forgotPassword(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                progressBar.setVisibility(View.GONE);
                tvResendCode.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(VerifyOtpActivity.this,
                            "Verification code sent successfully",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VerifyOtpActivity.this,
                            "Failed to resend code",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvResendCode.setEnabled(true);
                Toast.makeText(VerifyOtpActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
