package com.example.prm392_finalproject;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.prm392_finalproject.fragments.HomeFragment;
import com.example.prm392_finalproject.fragments.ProfileFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private MaterialToolbar toolbar;
    private FrameLayout toolbarContentContainer;
    private FragmentManager fragmentManager;

    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        setContentView(R.layout.activity_main_container);

        toolbar = findViewById(R.id.toolbar);
        toolbarContentContainer = findViewById(R.id.toolbarContentContainer);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fragmentManager = getSupportFragmentManager();

        // Add bottom padding to fragment container to prevent bottom nav from covering content
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        fragmentContainer.post(() -> {
            int bottomNavHeight = bottomNavigation.getHeight();
            fragmentContainer.setPadding(0, 0, 0, bottomNavHeight);
        });

        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            View appBarLayout = findViewById(R.id.appBarLayout);
            if (appBarLayout != null) {
                int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                appBarLayout.setPadding(0, topInset, 0, 0);
            }

            if (bottomNavigation != null) {
                int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                bottomNavigation.setPadding(0, 0, 0, bottomInset);
            }
            return insets;
        });

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            loadFragment(homeFragment);
            bottomNavigation.setSelectedItemId(R.id.nav_home);
            updateToolbarForHome();
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                loadFragment(homeFragment);
                updateToolbarForHome();
                return true;
            } else if (itemId == R.id.nav_orders) {
                updateToolbarForOrders();
                return true;
            } else if (itemId == R.id.nav_favorite) {
                updateToolbarForFavorites();
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (profileFragment == null) {
                    profileFragment = new ProfileFragment();
                }
                loadFragment(profileFragment);
                updateToolbarForProfile();
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void updateToolbarForHome() {
        toolbar.setTitle("");
        toolbarContentContainer.setVisibility(View.VISIBLE);
        toolbarContentContainer.removeAllViews();
        
        View homeToolbar = LayoutInflater.from(this).inflate(R.layout.toolbar_home, toolbarContentContainer, false);
        toolbarContentContainer.addView(homeToolbar);
        
        TabLayout tabLayout = homeToolbar.findViewById(R.id.tabLayout);
        if (tabLayout != null && homeFragment != null) {
            homeFragment.setTabLayout(tabLayout);
        }
    }

    private void updateToolbarForProfile() {
        toolbarContentContainer.setVisibility(View.GONE);
        toolbarContentContainer.removeAllViews();
        toolbar.setTitle("My Profile");
        toolbar.setTitleTextColor(Color.parseColor("#3B82F6"));
        setToolbarTitleBoldCentered();
    }

    private void updateToolbarForOrders() {
        toolbarContentContainer.setVisibility(View.GONE);
        toolbarContentContainer.removeAllViews();
        toolbar.setTitle("My Orders");
        toolbar.setTitleTextColor(Color.parseColor("#3B82F6"));
        setToolbarTitleBoldCentered();
    }

    private void updateToolbarForFavorites() {
        toolbarContentContainer.setVisibility(View.GONE);
        toolbarContentContainer.removeAllViews();
        toolbar.setTitle("Favorites");
        toolbar.setTitleTextColor(Color.parseColor("#3B82F6"));
        setToolbarTitleBoldCentered();
    }

    private void setToolbarTitleBoldCentered() {
        // Post to ensure toolbar has been laid out
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < toolbar.getChildCount(); i++) {
                    View view = toolbar.getChildAt(i);
                    if (view instanceof TextView) {
                        TextView textView = (TextView) view;
                        CharSequence title = toolbar.getTitle();
                        if (title != null && textView.getText().toString().equals(title.toString())) {
                            Toolbar.LayoutParams params = (Toolbar.LayoutParams) textView.getLayoutParams();
                            params.gravity = Gravity.CENTER;
                            textView.setLayoutParams(params);
                            textView.setTypeface(null, Typeface.BOLD);
                            textView.setTextSize(22); // Increase size for better visibility
                            break;
                        }
                    }
                }
            }
        });
    }
}