package com.example.prm392_finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_finalproject.models.PaymentStatus;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentStatusActivity extends AppCompatActivity {

    private ImageView imgStatus;
    private TextView tvTitle, tvMessage;
    private ProgressBar progressBar;
    private MaterialButton btnRetry, btnGoHome;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_status);

        imgStatus = findViewById(R.id.imgPaymentStatus);
        tvTitle = findViewById(R.id.tvPaymentStatusTitle);
        tvMessage = findViewById(R.id.tvPaymentStatusMessage);
        progressBar = findViewById(R.id.progressBar);
        btnRetry = findViewById(R.id.btnRetryPaymentCheck);
        btnGoHome = findViewById(R.id.btnGoHome);

        // btnRetry.setOnClickListener(v -> checkPaymentStatus());

        apiService = RetrofitClient.createService(ApiService.class);

        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentStatusActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // optional: closes PaymentStatusActivity so user can’t go back here
        });

        // Start checking payment when the activity opens
        Uri data = getIntent().getData();
        if (data != null) {
            String host = data.getPath();
            Log.d("paymentprocessor", host);
            if ("/vnpay".equalsIgnoreCase(host)) {
                Map<String, String> queryMap = extractVnPayQueryParams(data);
                forwardVnPayToBackend(queryMap);
            } else if ("/zalopay".equalsIgnoreCase(host)) {
                // handleZalopay(data);
            }
        }
    }

    private void handleZalopay(Uri data) {
        String result = data.getQueryParameter("result");
        String transactionId = data.getQueryParameter("zptrxid");
        Log.d("Payment", "ZaloPay result: " + result + " | zptrxid=" + transactionId);
    }

    private Map<String, String> extractVnPayQueryParams(Uri uri) {
        Map<String, String> params = new HashMap<>();
        for (String key : uri.getQueryParameterNames()) {
            if (key.startsWith("vnp_") == false)
                continue;
            Log.d("VnpayParam", key + ": " + uri.getQueryParameter(key));
            params.put(key, uri.getQueryParameter(key));
        }
        return params;
    }

    private void forwardVnPayToBackend(Map<String, String> params) {
        progressBar.setVisibility(View.VISIBLE);
        tvTitle.setText("Verifying payment...");
        Call<PaymentStatus> call = apiService.returnVnPay(params);
        call.enqueue(new Callback<PaymentStatus>() {
            @Override
            public void onResponse(Call<PaymentStatus> call, Response<PaymentStatus> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    PaymentStatus result = response.body();
                    Log.d("paymentResponse", response.body().toString());
                    if (result.isResult()) {
                        // Payment successful - Show notification
                        showSuccessNotification(result);

                        imgStatus.setImageResource(R.drawable.ic_payment_success);
                        tvTitle.setText("Payment Successful!");
                        tvMessage.setText("Your order ID: " + result.getOrderId());
                        btnGoHome.setVisibility(View.VISIBLE);
                    } else {
                        imgStatus.setImageResource(R.drawable.ic_payment_failed);
                        tvTitle.setText("Payment Failed");
                        tvMessage.setText("Reason: " + result.getMessage());
                        btnRetry.setVisibility(View.VISIBLE);
                    }
                } else {
                    imgStatus.setImageResource(R.drawable.ic_payment_failed);
                    tvTitle.setText("Payment Failed");
                    tvMessage.setText("Reason: " + response.message());
                    btnRetry.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<PaymentStatus> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                imgStatus.setImageResource(R.drawable.ic_payment_failed);
                tvTitle.setText("Payment Failed");
                tvMessage.setText("Reason: Cannot connect to the server");
                btnRetry.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Show success notification when payment is confirmed
     */
    private void showSuccessNotification(PaymentStatus paymentStatus) {
        try {
            // Get order amount from payment status or use 0 as fallback
            double orderAmount = paymentStatus.getAmount() != null ? paymentStatus.getAmount() : 0.0;

            NotificationHelper.showOrderPlacedNotification(
                    this,
                    String.valueOf(paymentStatus.getOrderId()),
                    orderAmount);

            Log.d("PaymentStatus", "Notification shown for Order ID: " + paymentStatus.getOrderId());
        } catch (Exception e) {
            Log.e("PaymentStatus", "Error showing notification", e);
        }
    }

}
