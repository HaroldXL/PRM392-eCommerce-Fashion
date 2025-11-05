package com.example.prm392_finalproject.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.prm392_finalproject.AboutUsActivity;
import com.example.prm392_finalproject.CategoryProductsActivity;
import com.example.prm392_finalproject.ProductDetailActivity;
import com.example.prm392_finalproject.SearchActivity;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.adapters.BannerAdapter;
import com.example.prm392_finalproject.adapters.CategoryAdapter;
import com.example.prm392_finalproject.adapters.ProductAdapter;
import com.example.prm392_finalproject.models.Category;
import com.example.prm392_finalproject.models.ProductListResponse;
import com.example.prm392_finalproject.models.ProductResponse;
import com.example.prm392_finalproject.models.User;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private ViewPager2 bannerViewPager;
    private LinearLayout dotsIndicator, homeContent, categoryContent;
    private RecyclerView rvProducts, rvCategories;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private ProgressBar progressBar;
    private ImageView btnSearch;
    private TextView tvGreeting, tvUserAvatar;
    private TabLayout tabLayout;
    private com.google.android.material.button.MaterialButton btnAboutUs;

    private ApiService apiService;
    private SessionManager sessionManager;
    private BannerAdapter bannerAdapter;

    private int currentPage = 1;
    private int pageSize = 10;

    private Handler bannerHandler = new Handler();
    private int currentBannerPosition = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupBanner();
        setupRecyclerViews();
        setupListeners();

        apiService = RetrofitClient.createService(ApiService.class);
        sessionManager = new SessionManager(requireContext());

        loadUserInfo();
        loadProducts();
    }

    private void initViews(View view) {
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        dotsIndicator = view.findViewById(R.id.dotsIndicator);
        homeContent = view.findViewById(R.id.homeContent);
        categoryContent = view.findViewById(R.id.categoryContent);
        rvProducts = view.findViewById(R.id.rvProducts);
        rvCategories = view.findViewById(R.id.rvCategories);
        progressBar = view.findViewById(R.id.progressBar);

        // TabLayout, UserHeader buttons will be set from MainActivity toolbar
    }

    // Method called from MainActivity to set TabLayout from toolbar
    public void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
        if (tabLayout != null) {
            setupTabLayout();

            // Also get user header views from toolbar
            View toolbarParent = (View) tabLayout.getParent();
            if (toolbarParent != null) {
                btnSearch = toolbarParent.findViewById(R.id.btnSearch);
                btnAboutUs = toolbarParent.findViewById(R.id.btnAboutUs);
                tvUserAvatar = toolbarParent.findViewById(R.id.tvUserAvatar);
                tvGreeting = toolbarParent.findViewById(R.id.tvGreeting);

                // Set up button listeners
                if (btnSearch != null) {
                    btnSearch.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), SearchActivity.class);
                        startActivity(intent);
                    });
                }

                if (btnAboutUs != null) {
                    btnAboutUs.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), AboutUsActivity.class);
                        startActivity(intent);
                    });
                }
            }
        }
    }

    private void setupBanner() {
        bannerAdapter = new BannerAdapter();
        bannerViewPager.setAdapter(bannerAdapter);

        setupDotsIndicator(bannerAdapter.getItemCount());
        updateDots(0);

        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentBannerPosition = position;
                updateDots(position);
            }
        });

        startBannerAutoScroll();
    }

    private void setupDotsIndicator(int count) {
        dotsIndicator.removeAllViews();
        ImageView[] dots = new ImageView[count];

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(requireContext());
            dots[i].setImageResource(R.drawable.dot_inactive);
            dots[i].setLayoutParams(params);
            dotsIndicator.addView(dots[i]);
        }
    }

    private void updateDots(int position) {
        int childCount = dotsIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView dot = (ImageView) dotsIndicator.getChildAt(i);
            if (i == position) {
                dot.setImageResource(R.drawable.dot_active);
            } else {
                dot.setImageResource(R.drawable.dot_inactive);
            }
        }
    }

    private void startBannerAutoScroll() {
        bannerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentBannerPosition == bannerAdapter.getItemCount() - 1) {
                    currentBannerPosition = 0;
                } else {
                    currentBannerPosition++;
                }
                bannerViewPager.setCurrentItem(currentBannerPosition, true);
                bannerHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void setupRecyclerViews() {
        // Products RecyclerView (Grid - 2 columns)
        productAdapter = new ProductAdapter(requireContext());
        GridLayoutManager productLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvProducts.setLayoutManager(productLayoutManager);
        rvProducts.setAdapter(productAdapter);

        productAdapter.setOnProductClickListener(product -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
            startActivity(intent);
        });

        // Categories RecyclerView (Vertical)
        categoryAdapter = new CategoryAdapter(requireContext());
        LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(requireContext());
        rvCategories.setLayoutManager(categoryLayoutManager);
        rvCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(category -> {
            Intent intent = new Intent(requireContext(), CategoryProductsActivity.class);
            intent.putExtra("category_id", category.getId());
            intent.putExtra("category_name", category.getName());
            startActivity(intent);
        });
    }

    private void setupListeners() {
        // Removed See All button listener
    }

    private void setupTabLayout() {
        if (tabLayout == null)
            return;

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showHomeContent();
                } else if (tab.getPosition() == 1) {
                    showCategoryContent();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void showHomeContent() {
        homeContent.setVisibility(View.VISIBLE);
        categoryContent.setVisibility(View.GONE);
    }

    private void showCategoryContent() {
        homeContent.setVisibility(View.GONE);
        categoryContent.setVisibility(View.VISIBLE);

        if (categoryAdapter.getItemCount() == 0) {
            loadCategories();
        }
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    categoryAdapter.setCategories(categories);
                } else {
                    Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserInfo() {
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            apiService.getUserProfile("Bearer " + token).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        if (tvGreeting != null) {
                            tvGreeting.setText("Hi, " + user.getFullName());
                        }
                        // Set avatar with first letter
                        if (tvUserAvatar != null && user.getFullName() != null && !user.getFullName().isEmpty()) {
                            String firstLetter = user.getFullName().substring(0, 1).toUpperCase();
                            tvUserAvatar.setText(firstLetter);
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    if (tvGreeting != null) {
                        tvGreeting.setText("Hi, User");
                    }
                    if (tvUserAvatar != null) {
                        tvUserAvatar.setText("U");
                    }
                }
            });
        } else {
            if (tvGreeting != null) {
                tvGreeting.setText("Hi, User");
            }
        }
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getProducts(currentPage, pageSize, null, null).enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ProductListResponse productResponse = response.body();
                    productAdapter.setProducts(productResponse.getItems());
                } else {
                    Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bannerHandler.removeCallbacksAndMessages(null);
    }
}
