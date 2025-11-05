package com.example.prm392_finalproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.prm392_finalproject.models.CartItem;
import com.example.prm392_finalproject.models.ProductDetail;
import com.example.prm392_finalproject.models.ProductVariant;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.CartManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private Toolbar toolbar;
    private ImageView imgProductDetail;
    private TextView tvProductName;
    private TextView tvCategoryName;
    private TextView tvProductPrice;
    private TextView tvProductDescription;
    private ChipGroup chipGroupSize;
    private ChipGroup chipGroupColor;
    private CardView cardStockInfo;
    private TextView tvStockQuantity;
    private Button btnAddToCart;
    private ProgressBar progressBar;

    private ApiService apiService;
    private CartManager cartManager;
    private ProductDetail productDetail;
    private String selectedSize;
    private String selectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        setupToolbar();

        apiService = RetrofitClient.createService(ApiService.class);
        cartManager = new CartManager(this);

        int productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (productId != -1) {
            loadProductDetail(productId);
        } else {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imgProductDetail = findViewById(R.id.imgProductDetail);
        tvProductName = findViewById(R.id.tvProductName);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        chipGroupSize = findViewById(R.id.chipGroupSize);
        chipGroupColor = findViewById(R.id.chipGroupColor);
        cardStockInfo = findViewById(R.id.cardStockInfo);
        tvStockQuantity = findViewById(R.id.tvStockQuantity);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupListeners() {
        chipGroupSize.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedSize = selectedChip.getText().toString();
                updateColorOptions();
                updateStockInfo();
            }
        });

        chipGroupColor.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedColor = selectedChip.getText().toString();
                updateStockInfo();
            }
        });

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadProductDetail(int productId) {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getProductDetail(productId).enqueue(new Callback<ProductDetail>() {
            @Override
            public void onResponse(Call<ProductDetail> call, Response<ProductDetail> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    productDetail = response.body();

                    // Debug: Log product variants
                    if (productDetail.getProductVariants() != null) {
                        for (ProductVariant variant : productDetail.getProductVariants()) {
                            android.util.Log.d("ProductDetail", "Variant loaded - ID: " + variant.getId() +
                                    ", Size: " + variant.getSize() + ", Color: " + variant.getColor());
                        }
                    }

                    displayProductDetail();
                } else {
                    Toast.makeText(ProductDetailActivity.this,
                            "Failed to load product details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDetail> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductDetailActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProductDetail() {
        // Set product info
        tvProductName.setText(productDetail.getName());
        tvCategoryName.setText(productDetail.getCategoryName());
        tvProductDescription.setText(productDetail.getDescription());

        // Format and set price
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvProductPrice.setText(formatter.format(productDetail.getPrice()) + "đ");

        // Load image
        Glide.with(this)
                .load(productDetail.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imgProductDetail);

        // Setup size and color options
        if (productDetail.getProductVariants() != null && !productDetail.getProductVariants().isEmpty()) {
            setupSizeOptions();
        }
    }

    private void setupSizeOptions() {
        chipGroupSize.removeAllViews();

        // Get unique sizes
        Set<String> sizes = new HashSet<>();
        for (ProductVariant variant : productDetail.getProductVariants()) {
            sizes.add(variant.getSize());
        }

        // Create chips for each size
        List<String> sortedSizes = new ArrayList<>(sizes);
        sortedSizes.sort((s1, s2) -> {
            // Custom sort: XL, 2XL, 3XL, 4XL, 5XL
            return compareSizes(s1, s2);
        });

        for (String size : sortedSizes) {
            Chip chip = new Chip(this);
            chip.setText(size);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color_selector, null));
            chip.setChipStrokeWidth(0);
            chip.setChipCornerRadius(20);
            chipGroupSize.addView(chip);
        }

        // Auto-select first size
        if (chipGroupSize.getChildCount() > 0) {
            ((Chip) chipGroupSize.getChildAt(0)).setChecked(true);
        }
    }

    private void updateColorOptions() {
        chipGroupColor.removeAllViews();

        if (selectedSize == null)
            return;

        // Get colors available for selected size
        Set<String> colors = new HashSet<>();
        for (ProductVariant variant : productDetail.getProductVariants()) {
            if (variant.getSize().equals(selectedSize)) {
                colors.add(variant.getColor());
            }
        }

        // Create chips for each color
        for (String color : colors) {
            Chip chip = new Chip(this);
            chip.setText(color);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color_selector, null));
            chip.setChipStrokeWidth(0);
            chip.setChipCornerRadius(20);
            chipGroupColor.addView(chip);
        }

        // Auto-select first color
        if (chipGroupColor.getChildCount() > 0) {
            ((Chip) chipGroupColor.getChildAt(0)).setChecked(true);
        }
    }

    private void updateStockInfo() {
        if (selectedSize == null || selectedColor == null) {
            cardStockInfo.setVisibility(View.GONE);
            return;
        }

        // Find the variant with selected size and color
        for (ProductVariant variant : productDetail.getProductVariants()) {
            if (variant.getSize().equals(selectedSize) && variant.getColor().equals(selectedColor)) {
                cardStockInfo.setVisibility(View.VISIBLE);
                tvStockQuantity.setText(variant.getStockQuantity() + " items");

                // Update button state based on stock
                if (variant.getStockQuantity() > 0) {
                    btnAddToCart.setEnabled(true);
                    btnAddToCart.setText("Add to Cart");
                    tvStockQuantity.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    btnAddToCart.setEnabled(false);
                    btnAddToCart.setText("Out of Stock");
                    tvStockQuantity.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
                return;
            }
        }

        cardStockInfo.setVisibility(View.GONE);
    }

    private void addToCart() {
        if (selectedSize == null || selectedColor == null) {
            Toast.makeText(this, "Please select size and color", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the selected variant
        for (ProductVariant variant : productDetail.getProductVariants()) {
            if (variant.getSize().equals(selectedSize) && variant.getColor().equals(selectedColor)) {
                // Log variant ID for debugging
                android.util.Log.d("ProductDetail", "Selected variant ID: " + variant.getId());

                // Create cart item with variant information
                CartItem cartItem = new CartItem(
                        productDetail.getId(),
                        variant.getId(), // productVariantId
                        productDetail.getName(),
                        productDetail.getImageUrl(),
                        productDetail.getPrice(),
                        1, // Default quantity
                        selectedSize,
                        selectedColor);

                // Add to cart
                cartManager.addToCart(cartItem);

                Toast.makeText(this,
                        "Added to cart successfully!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(this, "Variant not found!", Toast.LENGTH_SHORT).show();
    }

    private int compareSizes(String s1, String s2) {
        // Extract numbers from size strings (e.g., "XL" = 1, "2XL" = 2, etc.)
        int num1 = s1.matches("\\d+XL") ? Integer.parseInt(s1.replaceAll("[^0-9]", "")) : 1;
        int num2 = s2.matches("\\d+XL") ? Integer.parseInt(s2.replaceAll("[^0-9]", "")) : 1;
        return Integer.compare(num1, num2);
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
