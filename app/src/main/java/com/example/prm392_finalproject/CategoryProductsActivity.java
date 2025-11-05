package com.example.prm392_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.adapters.ProductAdapter;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.models.ProductListResponse;
import com.example.prm392_finalproject.models.ProductResponse;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryProductsActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Toolbar toolbar;

    private int categoryId;
    private String categoryName;
    private Timer searchTimer;
    private static final long SEARCH_DELAY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        // Get category info from intent
        categoryId = getIntent().getIntExtra("category_id", 0);
        categoryName = getIntent().getStringExtra("category_name");

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadProducts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearchCategory);
        recyclerView = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName != null ? categoryName : "Products");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this);
        productAdapter.setProducts(productList);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(productAdapter);

        // Set click listener for product items
        productAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(CategoryProductsActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });

        setupSearchListener();
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> loadProducts(query));
                    }
                }, SEARCH_DELAY);
            }
        });
    }

    private void loadProducts() {
        loadProducts(null);
    }

    private void loadProducts(String searchQuery) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.createService(ApiService.class);
        Call<ProductListResponse> call = apiService.getProducts(1, 100, searchQuery, categoryId);

        call.enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ProductListResponse productResponse = response.body();

                    if (productResponse.getItems() != null && !productResponse.getItems().isEmpty()) {
                        productAdapter.setProducts(productResponse.getItems());

                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        showEmptyState();
                    }
                } else {
                    Toast.makeText(CategoryProductsActivity.this,
                            "Failed to load products", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ProductListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CategoryProductsActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchTimer != null) {
            searchTimer.cancel();
        }
    }
}
