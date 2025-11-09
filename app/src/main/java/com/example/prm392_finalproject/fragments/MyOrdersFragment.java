package com.example.prm392_finalproject.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.adapters.OrderAdapter;
import com.example.prm392_finalproject.models.Order;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyOrdersFragment extends Fragment implements OrderAdapter.OnOrderActionListener {
    private static final String TAG = "MyOrdersFragment";

    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private ProgressBar progressBar;
    private View emptyView;
    private TabLayout tabLayout;
    private SessionManager sessionManager;
    private ApiService apiService;

    private List<Order> allMyOrders = new ArrayList<>();
    private String currentTab = "Pending"; // "Pending", "Confirmed", "Delivering", "Completed", or "Cancelled"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewOrders);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        tabLayout = view.findViewById(R.id.tabLayout);

        // Initialize session and API
        sessionManager = new SessionManager(requireContext());
        apiService = RetrofitClient.createService(ApiService.class);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderAdapter = new OrderAdapter(new ArrayList<>());
        orderAdapter.setOnOrderActionListener(this);
        recyclerView.setAdapter(orderAdapter);

        // Setup TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentTab = "Pending";
                        break;
                    case 1:
                        currentTab = "Confirmed";
                        break;
                    case 2:
                        currentTab = "Delivering";
                        break;
                    case 3:
                        currentTab = "Completed";
                        break;
                    case 4:
                        currentTab = "Cancelled";
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
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
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
                    List<Order> allOrders = response.body();
                    allMyOrders.clear();
                    int currentUserId = sessionManager.getUserId();

                    // Filter orders by current user
                    for (Order order : allOrders) {
                        if (order.getUserId() == currentUserId) {
                            allMyOrders.add(order);
                        }
                    }

                    // Filter by status based on current tab
                    filterOrdersByStatus();

                    Log.d(TAG, "Loaded " + allMyOrders.size() + " orders for user " + currentUserId);
                } else {
                    Log.e(TAG, "Failed to load orders: " + response.code());
                    Toast.makeText(requireContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error loading orders", t);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOrdersByStatus() {
        List<Order> filteredOrders = new ArrayList<>();

        for (Order order : allMyOrders) {
            if (currentTab.equalsIgnoreCase(order.getStatus())) {
                filteredOrders.add(order);
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

    @Override
    public void onResume() {
        super.onResume();
        // Reload orders when fragment becomes visible
        loadOrders();
    }

    @Override
    public void onCancelOrder(Order order, int position) {
        // Show confirmation dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    performCancelOrder(order);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void performCancelOrder(Order order) {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Create JSON object for status update
        // API expects lowercase "cancelled"
        JsonObject statusUpdate = new JsonObject();
        statusUpdate.addProperty("status", "cancelled");

        apiService.updateOrderStatus(order.getId(), "Bearer " + token, statusUpdate)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                            // Reload orders to refresh the list
                            loadOrders();
                        } else {
                            Log.e(TAG, "Failed to cancel order: " + response.code());
                            Toast.makeText(requireContext(), "Failed to cancel order", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Error cancelling order", t);
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
