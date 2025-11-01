package com.example.prm392_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.adapters.CategoryAdapter;
import com.example.prm392_finalproject.adapters.ProductAdapter;
import com.example.prm392_finalproject.models.Category;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.models.ProductResponse;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvCategories, rvProducts;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private ProgressBar progressBar;
    private EditText etSearch;
    private MaterialButton btnLogout;
    private ImageView btnProfile;
    private TextView tvViewAllCategories, tvViewAllProducts;

    private ApiService apiService;
    private SessionManager sessionManager;

    private int currentPage = 1;
    private int pageSize = 10;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Setup toolbar as ActionBar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        initViews();
        setupRecyclerViews();
        setupListeners();

        apiService = RetrofitClient.createService(ApiService.class);
        sessionManager = new SessionManager(this);

        loadCategories();
        loadProducts();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        rvProducts = findViewById(R.id.rvProducts);
        progressBar = findViewById(R.id.progressBar);
        etSearch = findViewById(R.id.etSearch);
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);
        tvViewAllCategories = findViewById(R.id.tvViewAllCategories);
        tvViewAllProducts = findViewById(R.id.tvViewAllProducts);
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView (Horizontal)
        categoryAdapter = new CategoryAdapter(this);
        LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);
        rvCategories.setLayoutManager(categoryLayoutManager);
        rvCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(category -> {
            Toast.makeText(this, "Category: " + category.getName(), Toast.LENGTH_SHORT).show();
            // Load products by category
            loadProductsByCategory(category.getId());
        });

        // Products RecyclerView (Vertical)
        productAdapter = new ProductAdapter(this);
        LinearLayoutManager productLayoutManager = new LinearLayoutManager(this);
        rvProducts.setLayoutManager(productLayoutManager);
        rvProducts.setAdapter(productAdapter);

        productAdapter.setOnProductClickListener(product -> {
            Toast.makeText(this, "Product: " + product.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to Product Detail
        });
    }

    private void setupListeners() {
        // Profile button
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Logout button
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                loadProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // View All buttons
        tvViewAllCategories.setOnClickListener(v -> {
            Toast.makeText(this, "View All Categories", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to All Categories
        });

        tvViewAllProducts.setOnClickListener(v -> {
            Toast.makeText(this, "View All Products", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to All Products
        });
    }

    private void loadCategories() {
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryAdapter.setCategories(response.body());
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);

        String filterName = searchQuery.isEmpty() ? null : searchQuery;

        apiService.getProducts(currentPage, pageSize, filterName, null).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse productResponse = response.body();
                    productAdapter.setProducts(productResponse.getItems());
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductsByCategory(int categoryId) {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getProducts(currentPage, pageSize, null, categoryId).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse productResponse = response.body();
                    productAdapter.setProducts(productResponse.getItems());
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
