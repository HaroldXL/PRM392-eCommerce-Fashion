package com.example.prm392_finalproject.fragments;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.prm392_finalproject.OrderActivity;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.adapters.CartAdapter;
import com.example.prm392_finalproject.models.CartItem;
import com.example.prm392_finalproject.utils.CartManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private CartManager cartManager;
    private ProgressBar progressBar;
    private View emptyStateView;
    private MaterialCardView summaryCard;
    private TextView tvTotalPrice;
    private MaterialButton btnCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateView = view.findViewById(R.id.emptyStateLayout);
        summaryCard = view.findViewById(R.id.cartSummaryCard);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        // Initialize CartManager
        cartManager = new CartManager(requireContext());

        // Setup RecyclerView
        setupRecyclerView();

        // Setup checkout button
        btnCheckout.setOnClickListener(v -> proceedToCheckout());

        // Load cart items
        loadCartItems();

        return view;
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(cartAdapter);
    }

    private void loadCartItems() {
        progressBar.setVisibility(View.VISIBLE);

        // Simulate loading delay (you can remove this in production)
        recyclerView.postDelayed(() -> {
            List<CartItem> cartItems = cartManager.getCartItems();
            cartAdapter.setCartItems(cartItems);
            updateUI(cartItems);
            progressBar.setVisibility(View.GONE);
        }, 300);
    }

    private void updateUI(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            // Show empty state
            emptyStateView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            summaryCard.setVisibility(View.GONE);
        } else {
            // Show cart items
            emptyStateView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            summaryCard.setVisibility(View.VISIBLE);

            // Update total price in VND
            double totalPrice = cartManager.getTotalPrice();
            NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
            tvTotalPrice.setText(formatter.format(totalPrice) + "đ");
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        cartManager.updateQuantity(item.getProductId(), newQuantity);
        loadCartItems();
        Toast.makeText(requireContext(), "Quantity updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveItem(CartItem item) {
        cartManager.removeFromCart(item.getProductId());
        loadCartItems();
        Toast.makeText(requireContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
    }

    private void proceedToCheckout() {
        List<CartItem> cartItems = cartManager.getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to OrderActivity
        Intent intent = new Intent(requireContext(), OrderActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh cart when fragment becomes visible
        loadCartItems();
    }
}
