package com.example.prm392_finalproject.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.adapters.AdminOrderAdapter;
import com.example.prm392_finalproject.models.Order;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageOrdersActivity extends AppCompatActivity {
    private static final String TAG = "ManageOrdersActivity";

    private RecyclerView recyclerView;
    private AdminOrderAdapter orderAdapter;
    private ProgressBar progressBar;
    private View emptyView;
    private TabLayout tabLayout;
    private SessionManager sessionManager;
    private ApiService apiService;

    private List<Order> allOrders = new ArrayList<>();
    private String currentTab = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Orders");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewOrders);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        tabLayout = findViewById(R.id.tabLayout);

        // Initialize session and API
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.createService(ApiService.class);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new AdminOrderAdapter(new ArrayList<>(), this::onOrderAction);
        recyclerView.setAdapter(orderAdapter);

        // Setup TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "All";
                        break;
                    case 1:
                        currentTab = "Pending";
                        break;
                    case 2:
                        currentTab = "Confirmed";
                        break;
                    case 3:
                        currentTab = "Delivering";
                        break;
                    case 4:
                        currentTab = "Completed";
                        break;
                }
                filterOrdersByStatus();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Load orders
        loadOrders();
    }

    private void loadOrders() {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        apiService.getOrders("Bearer " + token).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body();
                    filterOrdersByStatus();
                    Log.d(TAG, "Loaded " + allOrders.size() + " orders");
                } else {
                    Log.e(TAG, "Failed to load orders: " + response.code());
                    Toast.makeText(ManageOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error loading orders", t);
                Toast.makeText(ManageOrdersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOrdersByStatus() {
        List<Order> filteredOrders = new ArrayList<>();

        if ("All".equals(currentTab)) {
            filteredOrders = new ArrayList<>(allOrders);
        } else {
            for (Order order : allOrders) {
                if (currentTab.equalsIgnoreCase(order.getStatus())) {
                    filteredOrders.add(order);
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            orderAdapter.setOrders(filteredOrders);
        }

        Log.d(TAG, "Filtered " + filteredOrders.size() + " orders with status: " + currentTab);
    }

    private void onOrderAction(String action, Order order) {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (action) {
            case "UPDATE_STATUS":
                updateOrderStatus(order, token);
                break;
            case "DELETE":
                deleteOrder(order.getId(), token);
                break;
        }
    }

    private void updateOrderStatus(Order order, String token) {
        String newStatus = getNextStatus(order.getStatus());
        if (newStatus == null) {
            Toast.makeText(this, "Order is already completed", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Create request body
        com.google.gson.JsonObject statusUpdate = new com.google.gson.JsonObject();
        statusUpdate.addProperty("status", newStatus);

        apiService.updateOrderStatus(order.getId(), "Bearer " + token, statusUpdate)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(ManageOrdersActivity.this,
                                    "Order updated to " + newStatus, Toast.LENGTH_SHORT).show();
                            loadOrders(); // Reload orders
                        } else {
                            Toast.makeText(ManageOrdersActivity.this,
                                    "Failed to update order", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ManageOrdersActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteOrder(int orderId, String token) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Order")
                .setMessage("Are you sure you want to delete this order?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    apiService.deleteOrder(orderId, "Bearer " + token)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    progressBar.setVisibility(View.GONE);
                                    if (response.isSuccessful()) {
                                        Toast.makeText(ManageOrdersActivity.this,
                                                "Order deleted", Toast.LENGTH_SHORT).show();
                                        loadOrders(); // Reload orders
                                    } else {
                                        Toast.makeText(ManageOrdersActivity.this,
                                                "Failed to delete order", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(ManageOrdersActivity.this,
                                            "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getNextStatus(String currentStatus) {
        // Valid transitions: Pending -> Confirmed -> Delivering -> Completed
        switch (currentStatus) {
            case "Pending":
                return "Confirmed";
            case "Confirmed":
                return "Delivering";
            case "Delivering":
                return "Completed";
            default:
                return null; // Already completed or invalid status
        }
    }
}
