package com.example.prm392_finalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.adapters.ChatAdapter;
import com.example.prm392_finalproject.models.Chat;
import com.example.prm392_finalproject.models.PagedResult;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserChatListActivity extends AppCompatActivity {

    private RecyclerView rvChats;
    private ChatAdapter chatAdapter;
    private Button btnPrevious, btnNext;
    private TextView tvPage;

    private String token;
    private List<Chat> allChats; // full chat list
    private List<Chat> currentPageChats; // chats for current page
    private ApiService apiService;
    private SessionManager sessionManager;

    private int currentPage = 1;
    private int totalPage = 1;
    private int pageSize = 10; // chats per page
    private int totalPages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat_list);

        rvChats = findViewById(R.id.rvChats);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        tvPage = findViewById(R.id.tvPage);

        sessionManager = new SessionManager(this);
        token = "Bearer " + sessionManager.getToken();
        apiService = RetrofitClient.createService(ApiService.class);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        totalPages = 1;

        currentPageChats = new ArrayList<>();
        chatAdapter = new ChatAdapter(currentPageChats, chat -> {
            openChatFragment(chat.getCustomerId());
        });
        rvChats.setAdapter(chatAdapter);

        loadPage(currentPage);

        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadPage(currentPage);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadPage(currentPage);
            }
        });

    }

    private void loadPage(int page) {
        // Show loading if needed
        int pageSize = this.pageSize;

        apiService.getUserChatList(token, page, pageSize).enqueue(new Callback<PagedResult<Chat>>() {
            @Override
            public void onResponse(Call<PagedResult<Chat>> call, Response<PagedResult<Chat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PagedResult<Chat> pagedResult = response.body();
                    currentPageChats.clear();
                    currentPageChats.addAll(pagedResult.getItems());
                    totalPages = (int) Math.ceil(pagedResult.getTotalCount() / pageSize);
                    chatAdapter.notifyDataSetChanged();
                    tvPage.setText(currentPage + " / " + totalPages);
                    btnPrevious.setEnabled(currentPage > 1);
                    btnNext.setEnabled(currentPage < totalPages);
                }
            }

            @Override
            public void onFailure(Call<PagedResult<Chat>> call, Throwable t) {
                Toast.makeText(UserChatListActivity.this, "Cannot get user list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChatFragment(int chatId) {
        ChatFragment fragment = new ChatFragment();

        // Pass arguments if needed
        Bundle args = new Bundle();
        args.putInt("target_customer_id", chatId);
        fragment.setArguments(args);

        // Replace current view with the chat fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.chat_fragment_container, fragment)
                .addToBackStack(null) // so user can press back
                .commit();

        findViewById(R.id.chat_fragment_container).setVisibility(View.VISIBLE);
    }

}