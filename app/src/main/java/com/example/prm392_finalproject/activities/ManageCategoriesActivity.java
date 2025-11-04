package com.example.prm392_finalproject.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.prm392_finalproject.adapters.CategoryManageAdapter;
import com.example.prm392_finalproject.models.Category;
import com.example.prm392_finalproject.models.CreateCategoryRequest;
import com.example.prm392_finalproject.models.UpdateCategoryRequest;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.CloudinaryHelper;
import com.example.prm392_finalproject.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageCategoriesActivity extends AppCompatActivity
        implements CategoryManageAdapter.OnCategoryActionListener {

    private static final int PICK_IMAGE_REQUEST = 1;

    private RecyclerView recyclerView;
    private CategoryManageAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnAddCategory;

    private ApiService apiService;
    private SessionManager sessionManager;

    private AlertDialog currentDialog;
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private boolean isEditMode = false;
    private Category editingCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        initViews();
        setupToolbar();
        setupRecyclerView();

        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        loadCategories();

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        swipeRefresh.setOnRefreshListener(this::loadCategories);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewCategories);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        setSupportActionBar(toolbar);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Categories");
        }
    }

    private void setupRecyclerView() {
        adapter = new CategoryManageAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    adapter.setCategories(response.body());
                } else {
                    Toast.makeText(ManageCategoriesActivity.this,
                            "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(ManageCategoriesActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCategoryDialog() {
        isEditMode = false;
        editingCategory = null;
        selectedImageUri = null;
        uploadedImageUrl = null;
        showCategoryDialog("Add Category", null);
    }

    @Override
    public void onEditCategory(Category category) {
        isEditMode = true;
        editingCategory = category;
        selectedImageUri = null;
        uploadedImageUrl = category.getImageUrl();
        showCategoryDialog("Edit Category", category);
    }

    private void showCategoryDialog(String title, Category category) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_category, null);

        ImageView ivCategoryPreview = dialogView.findViewById(R.id.ivCategoryPreview);
        MaterialButton btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        TextInputEditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        ProgressBar uploadProgress = dialogView.findViewById(R.id.uploadProgress);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

        // Populate if editing
        if (category != null) {
            etCategoryName.setText(category.getName());
            Glide.with(this)
                    .load(category.getImageUrl())
                    .placeholder(R.drawable.ic_category)
                    .into(ivCategoryPreview);
        }

        // Create dialog
        currentDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .create();

        // Select image button
        btnSelectImage.setOnClickListener(v -> openImagePicker());

        // Cancel button
        btnCancel.setOnClickListener(v -> currentDialog.dismiss());

        // Save button
        btnSave.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show();
                return;
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
                        saveCategory(name, imageUrl);
                    }

                    @Override
                    public void onError(String error) {
                        uploadProgress.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(ManageCategoriesActivity.this,
                                "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(int progress) {
                        uploadProgress.setProgress(progress);
                    }
                });
            } else {
                // No new image selected, use existing URL
                saveCategory(name, uploadedImageUrl != null ? uploadedImageUrl : "");
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

            // Update preview in dialog
            if (currentDialog != null && currentDialog.isShowing()) {
                ImageView ivPreview = currentDialog.findViewById(R.id.ivCategoryPreview);
                if (ivPreview != null) {
                    Glide.with(this)
                            .load(selectedImageUri)
                            .into(ivPreview);
                }
            }
        }
    }

    private void saveCategory(String name, String imageUrl) {
        String token = "Bearer " + sessionManager.getToken();

        if (isEditMode && editingCategory != null) {
            // Update existing category
            UpdateCategoryRequest request = new UpdateCategoryRequest(name, imageUrl);
            apiService.updateCategory(editingCategory.getId(), token, request).enqueue(new Callback<Category>() {
                @Override
                public void onResponse(Call<Category> call, Response<Category> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ManageCategoriesActivity.this,
                                "Category updated successfully", Toast.LENGTH_SHORT).show();
                        currentDialog.dismiss();
                        loadCategories();
                    } else {
                        Toast.makeText(ManageCategoriesActivity.this,
                                "Failed to update category", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Category> call, Throwable t) {
                    Toast.makeText(ManageCategoriesActivity.this,
                            "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new category
            CreateCategoryRequest request = new CreateCategoryRequest(name, imageUrl);
            apiService.createCategory(token, request).enqueue(new Callback<Category>() {
                @Override
                public void onResponse(Call<Category> call, Response<Category> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ManageCategoriesActivity.this,
                                "Category created successfully", Toast.LENGTH_SHORT).show();
                        currentDialog.dismiss();
                        loadCategories();
                    } else {
                        Toast.makeText(ManageCategoriesActivity.this,
                                "Failed to create category", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Category> call, Throwable t) {
                    Toast.makeText(ManageCategoriesActivity.this,
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
}
