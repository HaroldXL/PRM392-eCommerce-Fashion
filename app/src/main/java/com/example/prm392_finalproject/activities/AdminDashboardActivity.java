package com.example.prm392_finalproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.prm392_finalproject.LoginActivity;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class AdminDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private CardView cardManageUsers, cardManageProducts, cardManageCategories;
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);

        // Check if user is logged in and is admin/manager
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardManageProducts = findViewById(R.id.cardManageProducts);
        cardManageCategories = findViewById(R.id.cardManageCategories);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupListeners() {
        cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageUsersActivity.class);
            startActivity(intent);
        });

        cardManageProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageProductsActivity.class);
            startActivity(intent);
        });

        cardManageCategories.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageCategoriesActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        sessionManager.logout();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
