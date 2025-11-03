package com.example.prm392_finalproject;

import android.os.Bundle;
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
import com.example.prm392_finalproject.models.CreateOrderRequest;
import com.example.prm392_finalproject.models.MessageResponse;
import com.example.prm392_finalproject.models.Order;
import com.example.prm392_finalproject.models.OrderItem;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.CartManager;
import com.example.prm392_finalproject.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        initViews();
        setupToolbar();

        cartManager = new CartManager(this);
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.createService(ApiService.class);

        loadCartData();
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

        // Get payment method
        String paymentMethod = "COD"; // Default
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == R.id.rbBankTransfer) {
            paymentMethod = "Bank Transfer";
        } else if (selectedId == R.id.rbCreditCard) {
            paymentMethod = "Credit Card";
        }

        // Create order object
        Order order = new Order(
                sessionManager.getUserId(),
                shippingName,
                shippingPhone,
                shippingAddress,
                paymentMethod);

        // Create order items (Note: we need productVariantId from cart, but CartItem
        // only has productId)
        // For now, using productId as productVariantId - you may need to adjust this
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(
                    cartItem.getProductId(), // Should be productVariantId
                    cartItem.getPrice(),
                    cartItem.getQuantity());
            orderItems.add(orderItem);
        }

        CreateOrderRequest request = new CreateOrderRequest(order, orderItems);

        // Call API
        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        String token = "Bearer " + sessionManager.getToken();
        apiService.createOrder(token, request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(OrderActivity.this,
                            "Order placed successfully!",
                            Toast.LENGTH_LONG).show();

                    // Clear cart
                    cartManager.clearCart();

                    // Go back to main screen
                    finish();
                } else {
                    Toast.makeText(OrderActivity.this,
                            "Failed to place order. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
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
}
