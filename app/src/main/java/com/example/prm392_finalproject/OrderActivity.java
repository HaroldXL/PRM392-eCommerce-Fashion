package com.example.prm392_finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.prm392_finalproject.models.CartItem;
import com.example.prm392_finalproject.models.PaymentInitRequest;
import com.example.prm392_finalproject.models.PaymentInitResponse;
import com.example.prm392_finalproject.models.User;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.CartManager;
import com.example.prm392_finalproject.utils.SessionManager;
import com.example.prm392_finalproject.utils.ZaloPayHelper;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class OrderActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etShippingName, etShippingPhone, etShippingAddress;
    private RadioGroup rgPaymentMethod;
    private TextView tvTotalItems, tvTotalAmount;
    private MaterialButton btnPlaceOrder;
    private ProgressBar progressBar;

    private CartManager cartManager;
    private SessionManager sessionManager;
    private ApiService apiService;
    private List<CartItem> cartItems;
    private double totalAmount;
    private int currentUserId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        initViews();
        setupToolbar();

        cartManager = new CartManager(this);
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.createService(ApiService.class);

        // Initialize ZaloPay SDK
        ZaloPayHelper.init(this);

        loadCartData();
        loadUserProfile();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etShippingName = findViewById(R.id.etShippingName);
        etShippingPhone = findViewById(R.id.etShippingPhone);
        etShippingAddress = findViewById(R.id.etShippingAddress);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void loadCartData() {
        cartItems = cartManager.getCartItems();

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Calculate total
        int totalItems = 0;
        totalAmount = 0;
        for (CartItem item : cartItems) {
            totalItems += item.getQuantity();
            totalAmount += item.getTotalPrice();
        }

        // Display totals
        tvTotalItems.setText(String.valueOf(totalItems));
        NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        tvTotalAmount.setText(formatter.format(totalAmount) + "đ");
    }

    private void setupListeners() {
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void loadUserProfile() {
        String token = "Bearer " + sessionManager.getToken();
        apiService.getUserProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    currentUserId = user.getId();

                    // Pre-fill user information
                    etShippingName.setText(user.getFullName());
                    etShippingPhone.setText(user.getPhone());
                    etShippingAddress.setText(user.getAddress());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Silently fail, user can still enter manually
            }
        });
    }

    private void placeOrder() {
        // Validate inputs
        String shippingName = etShippingName.getText().toString().trim();
        String shippingPhone = etShippingPhone.getText().toString().trim();
        String shippingAddress = etShippingAddress.getText().toString().trim();

        if (shippingName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            etShippingName.requestFocus();
            return;
        }

        if (shippingPhone.isEmpty()) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            etShippingPhone.requestFocus();
            return;
        }

        if (shippingAddress.isEmpty()) {
            Toast.makeText(this, "Please enter your address", Toast.LENGTH_SHORT).show();
            etShippingAddress.requestFocus();
            return;
        }

        if (currentUserId == 0) {
            Toast.makeText(this, "Please wait while loading user information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get payment method - VNPay or ZaloPay
        String paymentMethod = "VNPay"; // Default
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == R.id.rbZaloPay) {
            paymentMethod = "ZaloPay";
        }

        // Create payment items from cart
        List<PaymentInitRequest.PaymentItem> paymentItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            // Debug log
            android.util.Log.d("OrderActivity", "Cart item - ProductVariantId: " +
                    cartItem.getProductVariantId() + ", Amount: " + cartItem.getQuantity());

            PaymentInitRequest.PaymentItem item = new PaymentInitRequest.PaymentItem(
                    cartItem.getProductVariantId(),
                    cartItem.getQuantity()); // amount = quantity
            paymentItems.add(item);
        }

        // Create payment request
        PaymentInitRequest request = new PaymentInitRequest(
                currentUserId,
                paymentMethod,
                shippingName,
                shippingPhone,
                shippingAddress,
                paymentItems);

        // Call Payment API
        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        String token = "Bearer " + sessionManager.getToken();
        apiService.initPayment(token, request).enqueue(new Callback<PaymentInitResponse>() {
            @Override
            public void onResponse(Call<PaymentInitResponse> call, Response<PaymentInitResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    PaymentInitResponse paymentResponse = response.body();

                    // Check payment provider
                    if ("ZaloPay".equalsIgnoreCase(paymentResponse.getProvider())) {
                        // Handle ZaloPay payment
                        handleZaloPayPayment(paymentResponse);
                    } else {
                        // Handle VNPay or other payment methods
                        handleOtherPayment(paymentResponse);
                    }
                } else {
                    Toast.makeText(OrderActivity.this,
                            "Failed to initialize payment. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentInitResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(OrderActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle ZaloPay payment using SDK
     */
    private void handleZaloPayPayment(PaymentInitResponse paymentResponse) {
        try {
            String checkoutUrl = paymentResponse.getCheckoutUrl();
            String zpTransToken = extractZpTransToken(checkoutUrl);

            if (zpTransToken != null) {
                Toast.makeText(this, "Opening ZaloPay...", Toast.LENGTH_SHORT).show();

                ZaloPayHelper.payOrder(this, zpTransToken, new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                        cartManager.clearCart();
                        Toast.makeText(OrderActivity.this,
                                "Payment successful! Transaction ID: " + transactionId,
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onPaymentCanceled(String zpTransToken, String appTransID) {
                        Toast.makeText(OrderActivity.this,
                                "Payment canceled",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                        Toast.makeText(OrderActivity.this,
                                "Payment error: " + zaloPayError.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Fallback: open in browser if can't extract token
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
                startActivity(browserIntent);
                cartManager.clearCart();
                new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 2000);
            }
        } catch (Exception e) {
            Log.e("OrderActivity", "Error handling ZaloPay payment", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle other payment methods (VNPay, etc.)
     */
    private void handleOtherPayment(PaymentInitResponse paymentResponse) {
        cartManager.clearCart();

        if (paymentResponse.getCheckoutUrl() != null && !paymentResponse.getCheckoutUrl().isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(paymentResponse.getCheckoutUrl()));
            startActivity(browserIntent);

            Toast.makeText(this, "Redirecting to payment gateway...", Toast.LENGTH_LONG).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 2000);
        } else {
            Toast.makeText(this,
                    "Payment initiated successfully! Order ID: " + paymentResponse.getOrderId(),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Extract zpTransToken from ZaloPay checkout URL
     * URL format: https://qcgateway.zalopay.vn/openinapp?order=BASE64_TOKEN
     */
    private String extractZpTransToken(String checkoutUrl) {
        try {
            Uri uri = Uri.parse(checkoutUrl);
            String orderParam = uri.getQueryParameter("order");

            if (orderParam != null) {
                // Decode base64
                byte[] decodedBytes = android.util.Base64.decode(orderParam, android.util.Base64.DEFAULT);
                String decoded = new String(decodedBytes);

                // Parse JSON to get zptranstoken
                JSONObject json = new JSONObject(decoded);
                return json.getString("zptranstoken");
            }
        } catch (Exception e) {
            Log.e("OrderActivity", "Error extracting zpTransToken", e);
        }
        return null;
    }
}
