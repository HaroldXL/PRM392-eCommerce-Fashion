package com.example.prm392_finalproject.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.adapters.UserAdapter;
import com.example.prm392_finalproject.models.User;
import com.example.prm392_finalproject.models.UserResponse;
import com.example.prm392_finalproject.models.UpdateUserRequest;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageUsersActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnLoadMore;

    private ApiService apiService;
    private SessionManager sessionManager;

    private int currentPage = 1;
    private int pageSize = 10;
    private int totalCount = 0;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadUsers();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnLoadMore = findViewById(R.id.btnLoadMore);

        setSupportActionBar(toolbar);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Users");
        }
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);

        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 1;
            loadUsers();
        });

        btnLoadMore.setOnClickListener(v -> {
            if (userAdapter.getItemCount() < totalCount && !isLoading) {
                currentPage++;
                loadUsers();
            }
        });
    }

    private void loadUsers() {
        if (isLoading)
            return;
        isLoading = true;

        if (currentPage == 1) {
            progressBar.setVisibility(View.VISIBLE);
        }

        String token = "Bearer " + sessionManager.getToken();
        android.util.Log.d("ManageUsers", "Loading users - Page: " + currentPage + ", Token: "
                + (sessionManager.getToken() != null ? "exists" : "null"));

        Call<UserResponse> call = apiService.getUsers(token, null, null, null, null, currentPage, pageSize);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    totalCount = userResponse.getTotalCount();

                    android.util.Log.d("ManageUsers",
                            "Success - Total: " + totalCount + ", Items: " + userResponse.getItems().size());

                    if (currentPage == 1) {
                        userAdapter.setUsers(userResponse.getItems());
                    } else {
                        userAdapter.addUsers(userResponse.getItems());
                    }

                    // Show/hide load more button
                    if (userAdapter.getItemCount() >= totalCount) {
                        btnLoadMore.setVisibility(View.GONE);
                    } else {
                        btnLoadMore.setVisibility(View.VISIBLE);
                        btnLoadMore.setText("Load More (" + userAdapter.getItemCount() + "/" + totalCount + ")");
                    }
                } else {
                    android.util.Log.e("ManageUsers",
                            "Error - Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(ManageUsersActivity.this,
                            "Failed to load users (Code: " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                android.util.Log.e("ManageUsers", "Network error: " + t.getMessage(), t);
                Toast.makeText(ManageUsersActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewUser(User user) {
        showEditUserDialog(user);
    }

    private void showEditUserDialog(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);

        // Initialize views
        TextView tvDialogAvatar = dialogView.findViewById(R.id.tvDialogAvatar);
        TextInputEditText etDialogEmail = dialogView.findViewById(R.id.etDialogEmail);
        TextInputEditText etDialogFullName = dialogView.findViewById(R.id.etDialogFullName);
        TextInputEditText etDialogPhone = dialogView.findViewById(R.id.etDialogPhone);
        TextInputEditText etDialogAddress = dialogView.findViewById(R.id.etDialogAddress);
        AutoCompleteTextView actvDialogRole = dialogView.findViewById(R.id.actvDialogRole);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

        // Set avatar
        if (user.getFullName() != null && !user.getFullName().isEmpty()) {
            String firstLetter = user.getFullName().substring(0, 1).toUpperCase();
            tvDialogAvatar.setText(firstLetter);
        } else {
            tvDialogAvatar.setText("U");
        }

        // Populate fields
        etDialogEmail.setText(user.getEmail());
        etDialogFullName.setText(user.getFullName());
        etDialogPhone.setText(user.getPhone());
        etDialogAddress.setText(user.getAddress());

        // Setup role dropdown
        String[] roles = { "Admin", "Customer", "Manager" };
        int[] roleValues = { 1, 2, 3 };
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roles);
        actvDialogRole.setAdapter(roleAdapter);

        // Set current role
        int currentRoleIndex = user.getRole() == 1 ? 0 : (user.getRole() == 2 ? 1 : 2);
        actvDialogRole.setText(roles[currentRoleIndex], false);

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit User")
                .setView(dialogView)
                .create();

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Save button
        btnSave.setOnClickListener(v -> {
            String email = etDialogEmail.getText().toString().trim();
            String fullName = etDialogFullName.getText().toString().trim();
            String phone = etDialogPhone.getText().toString().trim();
            String address = etDialogAddress.getText().toString().trim();
            String roleText = actvDialogRole.getText().toString();

            // Validate
            if (email.isEmpty() || fullName.isEmpty() || phone.isEmpty() || roleText.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get role value
            int role = user.getRole(); // default to current
            for (int i = 0; i < roles.length; i++) {
                if (roles[i].equals(roleText)) {
                    role = roleValues[i];
                    break;
                }
            }

            // Update user
            updateUser(user.getId(), email, fullName, phone, address, role, dialog);
        });

        dialog.show();
    }

    private void updateUser(int userId, String email, String fullName, String phone, String address, int role,
            AlertDialog dialog) {
        String token = "Bearer " + sessionManager.getToken();
        UpdateUserRequest request = new UpdateUserRequest(email, fullName, phone, address, role);

        apiService.updateUser(userId, token, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ManageUsersActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    // Reload users
                    currentPage = 1;
                    loadUsers();
                } else {
                    android.util.Log.e("ManageUsers", "Update failed - Code: " + response.code());
                    Toast.makeText(ManageUsersActivity.this, "Failed to update user (Code: " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                android.util.Log.e("ManageUsers", "Update error: " + t.getMessage(), t);
                Toast.makeText(ManageUsersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getFullName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(int userId) {
        progressBar.setVisibility(View.VISIBLE);
        String token = "Bearer " + sessionManager.getToken();

        Call<Void> call = apiService.deleteUser(userId, token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ManageUsersActivity.this,
                            "User deleted successfully",
                            Toast.LENGTH_SHORT).show();
                    currentPage = 1;
                    loadUsers();
                } else {
                    Toast.makeText(ManageUsersActivity.this,
                            "Failed to delete user",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
