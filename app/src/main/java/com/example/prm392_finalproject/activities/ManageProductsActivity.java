package com.example.prm392_finalproject.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.adapters.ProductManageAdapter;
import com.example.prm392_finalproject.adapters.VariantEditAdapter;
import com.example.prm392_finalproject.models.Category;
import com.example.prm392_finalproject.models.CreateProductRequest;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.models.ProductListResponse;
import com.example.prm392_finalproject.models.ProductVariant;
import com.example.prm392_finalproject.models.UpdateProductRequest;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.CloudinaryHelper;
import com.example.prm392_finalproject.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageProductsActivity extends AppCompatActivity implements ProductManageAdapter.OnProductActionListener {

    private static final int PICK_IMAGE_REQUEST = 1;

    private RecyclerView recyclerView;
    private ProductManageAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnAddProduct, btnFilter, btnPrevious, btnNext;
    private TextInputEditText etSearch;
    private TextView tvPageInfo;
    private View paginationLayout;

    private ApiService apiService;
    private SessionManager sessionManager;

    private AlertDialog currentDialog;
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private boolean isEditMode = false;
    private Product editingProduct;

    private List<Category> categories = new ArrayList<>();
    private Map<String, Integer> categoryMap = new HashMap<>();
    private int selectedCategoryId = -1;

    private Integer filterCategoryId = null;
    private String searchQuery = null;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Pagination
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalCount = 0;
    private int totalPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        initViews();
        setupToolbar();
        setupRecyclerView();

        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        loadCategories();
        loadProducts();

        btnAddProduct.setOnClickListener(v -> showAddProductDialog());
        btnFilter.setOnClickListener(v -> showFilterDialog());
        swipeRefresh.setOnRefreshListener(this::loadProducts);

        // Search with debounce
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    searchQuery = s.toString().trim().isEmpty() ? null : s.toString().trim();
                    currentPage = 1; // Reset to first page on search
                    loadProducts();
                };
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Pagination controls
        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadProducts();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadProducts();
            }
        });
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnFilter = findViewById(R.id.btnFilter);
        etSearch = findViewById(R.id.etSearch);
        paginationLayout = findViewById(R.id.paginationLayout);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        tvPageInfo = findViewById(R.id.tvPageInfo);

        setSupportActionBar(toolbar);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Products");
        }
    }

    private void setupRecyclerView() {
        adapter = new ProductManageAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadCategories() {
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    categoryMap.clear();
                    for (Category category : categories) {
                        categoryMap.put(category.getName(), category.getId());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(ManageProductsActivity.this,
                        "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getProducts(currentPage, pageSize, searchQuery, filterCategoryId)
                .enqueue(new Callback<ProductListResponse>() {
                    @Override
                    public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ProductListResponse responseBody = response.body();
                            List<Product> products = responseBody.getItems();
                            totalCount = responseBody.getTotalCount();
                            totalPages = (int) Math.ceil((double) totalCount / pageSize);

                            // Map categoryId to categoryName
                            if (products != null && !categories.isEmpty()) {
                                for (Product product : products) {
                                    for (Category category : categories) {
                                        if (category.getId() == product.getCategoryId()) {
                                            product.setCategoryName(category.getName());
                                            break;
                                        }
                                    }
                                }
                            }

                            adapter.setProducts(products);
                            updatePaginationControls();
                        } else {
                            Toast.makeText(ManageProductsActivity.this,
                                    "Failed to load products", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductListResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(ManageProductsActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePaginationControls() {
        if (totalPages > 1) {
            paginationLayout.setVisibility(View.VISIBLE);
            tvPageInfo.setText("Page " + currentPage + " of " + totalPages);
            btnPrevious.setEnabled(currentPage > 1);
            btnNext.setEnabled(currentPage < totalPages);
        } else {
            paginationLayout.setVisibility(View.GONE);
        }
    }

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_products, null);
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerFilterCategory);
        MaterialButton btnClear = dialogView.findViewById(R.id.btnClearFilter);
        MaterialButton btnApply = dialogView.findViewById(R.id.btnApplyFilter);

        // Setup category dropdown
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All Categories");
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        spinnerCategory.setAdapter(categoryAdapter);

        if (filterCategoryId != null) {
            for (Category cat : categories) {
                if (cat.getId() == filterCategoryId) {
                    spinnerCategory.setText(cat.getName(), false);
                    break;
                }
            }
        } else {
            spinnerCategory.setText("All Categories", false);
        }

        AlertDialog filterDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnClear.setOnClickListener(v -> {
            filterCategoryId = null;
            currentPage = 1;
            loadProducts();
            filterDialog.dismiss();
        });

        btnApply.setOnClickListener(v -> {
            String selected = spinnerCategory.getText().toString();
            if (selected.equals("All Categories")) {
                filterCategoryId = null;
            } else {
                filterCategoryId = categoryMap.get(selected);
            }
            currentPage = 1;
            loadProducts();
            filterDialog.dismiss();
        });

        filterDialog.show();
    }

    private void showAddProductDialog() {
        isEditMode = false;
        editingProduct = null;
        selectedImageUri = null;
        uploadedImageUrl = null;
        selectedCategoryId = -1;
        showProductDialog("Add Product", null);
    }

    @Override
    public void onEditProduct(Product product) {
        // Load full product details first
        progressBar.setVisibility(View.VISIBLE);
        String token = "Bearer " + sessionManager.getToken();

        apiService.getProductById(product.getId()).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    isEditMode = true;
                    editingProduct = response.body();
                    selectedImageUri = null;
                    uploadedImageUrl = editingProduct.getImageUrl();
                    selectedCategoryId = editingProduct.getCategoryId();
                    showProductDialog("Edit Product", editingProduct);
                } else {
                    Toast.makeText(ManageProductsActivity.this,
                            "Failed to load product details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageProductsActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProductDialog(String title, Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_product, null);

        ImageView ivProductPreview = dialogView.findViewById(R.id.ivProductPreview);
        MaterialButton btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        TextInputEditText etProductName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etPrice);
        RecyclerView recyclerViewVariants = dialogView.findViewById(R.id.recyclerViewVariants);
        MaterialButton btnAddVariant = dialogView.findViewById(R.id.btnAddVariant);
        ProgressBar uploadProgress = dialogView.findViewById(R.id.uploadProgress);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

        // Setup variants adapter
        VariantEditAdapter variantAdapter = new VariantEditAdapter(this);
        recyclerViewVariants.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewVariants.setAdapter(variantAdapter);

        // Setup category dropdown
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        spinnerCategory.setAdapter(categoryAdapter);

        // Populate if editing
        if (product != null) {
            etProductName.setText(product.getName());
            etDescription.setText(product.getDescription());
            etPrice.setText(String.valueOf(product.getPrice()));

            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_category)
                    .into(ivProductPreview);

            // Set category
            for (Category cat : categories) {
                if (cat.getId() == product.getCategoryId()) {
                    spinnerCategory.setText(cat.getName(), false);
                    break;
                }
            }

            // Set variants
            if (product.getProductVariants() != null) {
                variantAdapter.setVariants(product.getProductVariants());
            }
        }

        // Category selection
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = categoryNames.get(position);
            selectedCategoryId = categoryMap.get(selectedName);
        });

        // Create dialog
        currentDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .create();

        // Select image button
        btnSelectImage.setOnClickListener(v -> openImagePicker());

        // Add variant button
        btnAddVariant.setOnClickListener(v -> variantAdapter.addVariant());

        // Cancel button
        btnCancel.setOnClickListener(v -> currentDialog.dismiss());

        // Save button
        btnSave.setOnClickListener(v -> {
            String name = etProductName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter product name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (priceStr.isEmpty()) {
                Toast.makeText(this, "Please enter price", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCategoryId == -1) {
                Toast.makeText(this, "Please select category", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
                return;
            }

            List<ProductVariant> variants = variantAdapter.getVariants();
            if (variants.isEmpty()) {
                Toast.makeText(this, "Please add at least one variant", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate variants
            for (ProductVariant variant : variants) {
                if (variant.getSize().isEmpty() || variant.getColor().isEmpty()) {
                    Toast.makeText(this, "All variants must have size and color", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // If image selected, upload first
            if (selectedImageUri != null) {
                uploadProgress.setVisibility(View.VISIBLE);
                btnSave.setEnabled(false);

                CloudinaryHelper.uploadImage(this, selectedImageUri, new CloudinaryHelper.CloudinaryUploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        uploadProgress.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        uploadedImageUrl = imageUrl;
                        saveProduct(name, description, price, imageUrl, variants);
                    }

                    @Override
                    public void onError(String error) {
                        uploadProgress.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(ManageProductsActivity.this,
                                "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(int progress) {
                        uploadProgress.setProgress(progress);
                    }
                });
            } else {
                saveProduct(name, description, price, uploadedImageUrl != null ? uploadedImageUrl : "", variants);
            }
        });

        currentDialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            if (currentDialog != null && currentDialog.isShowing()) {
                ImageView ivPreview = currentDialog.findViewById(R.id.ivProductPreview);
                if (ivPreview != null) {
                    Glide.with(this)
                            .load(selectedImageUri)
                            .into(ivPreview);
                }
            }
        }
    }

    private void saveProduct(String name, String description, double price, String imageUrl,
            List<ProductVariant> variants) {
        String token = "Bearer " + sessionManager.getToken();

        if (isEditMode && editingProduct != null) {
            UpdateProductRequest request = new UpdateProductRequest(name, description, imageUrl, price, variants);
            apiService.updateProduct(editingProduct.getId(), token, request).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ManageProductsActivity.this,
                                "Product updated successfully", Toast.LENGTH_SHORT).show();
                        currentDialog.dismiss();
                        loadProducts();
                    } else {
                        Toast.makeText(ManageProductsActivity.this,
                                "Failed to update product", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    Toast.makeText(ManageProductsActivity.this,
                            "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            CreateProductRequest request = new CreateProductRequest(selectedCategoryId, name, description, imageUrl,
                    price, variants);
            apiService.createProduct(token, request).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ManageProductsActivity.this,
                                "Product created successfully", Toast.LENGTH_SHORT).show();
                        currentDialog.dismiss();
                        loadProducts();
                    } else {
                        Toast.makeText(ManageProductsActivity.this,
                                "Failed to create product", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    Toast.makeText(ManageProductsActivity.this,
                            "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}
