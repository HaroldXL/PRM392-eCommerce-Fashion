package com.example.prm392_finalproject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.adapters.ChatMessageAdapter;
import com.example.prm392_finalproject.models.ChatMessage;
import com.example.prm392_finalproject.models.PagedResult;
import com.example.prm392_finalproject.models.SendMessageRequest;
import com.example.prm392_finalproject.network.ApiConfig;
import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;
import com.example.prm392_finalproject.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerChat;
    private TextInputEditText edtMessage;
    private MaterialButton btnSend;
    private ChatMessageAdapter adapter;
    private List<ChatMessage> messages;
    private int currentUserId; // Replace with your logged-in user’s ID
    private int targetedCustomerId;
    private boolean isStaff;

    private ApiService apiService;
    private int currentPage = 1;
    private final int pageSize = 20;
    private SessionManager sessionManager;
    private String token;
    private Thread subscriptionThread;
    private Thread sseThread;
    private volatile boolean shouldContinue = false;

    private long lastMessage = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerChat = view.findViewById(R.id.recyclerChat);
        edtMessage = view.findViewById(R.id.edtMessage);
        btnSend = view.findViewById(R.id.btnSend);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);   // Stack from bottom
        recyclerChat.setLayoutManager(layoutManager);

        // Hide bottom nav and toolbar when keyboard shows
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (imeInsets.bottom > 0) {
                // Adjust input section padding for keyboard
                LinearLayout inputSection = view.findViewById(R.id.inputSection);
                inputSection.setPadding(
                        inputSection.getPaddingLeft(),
                        inputSection.getPaddingTop(),
                        inputSection.getPaddingRight(),
                        imeInsets.bottom * 4/5 - systemBars.bottom
                );

                // Scroll to bottom
                recyclerChat.postDelayed(() ->
                        recyclerChat.smoothScrollToPosition(messages.size() - 1), 100);
            } else {
                LinearLayout inputSection = view.findViewById(R.id.inputSection);
                inputSection.setPadding(
                        inputSection.getPaddingLeft(),
                        inputSection.getPaddingTop(),
                        inputSection.getPaddingRight(),
                        0
                );
            }
            return insets;
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messages = new Stack<>();
        sessionManager = new SessionManager(requireContext());
        currentUserId = sessionManager.getUserId();
        adapter = new ChatMessageAdapter(messages, currentUserId);
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(adapter);
        apiService = RetrofitClient.createService(ApiService.class);
        token = "Bearer " + sessionManager.getToken();
        // Get arguments passed from Activity
        if (getArguments() != null) {
            targetedCustomerId = getArguments().getInt("target_customer_id", -1);
            isStaff = (targetedCustomerId != -1 );
        }

        loadMessages();
        btnSend.setOnClickListener(v -> {
            String message = edtMessage.getText().toString();
            if (message.isEmpty()) return;
            sendMessage(message);
            edtMessage.setText("");
        });

    }

    private void loadMessages() {
        Call<PagedResult<ChatMessage>> call;
        if (isStaff) {
            call = apiService.getCustomerMessage(targetedCustomerId, token, currentPage, pageSize);
        } else {
            call = apiService.getCurrentCustomerMessage(token, currentPage, pageSize);
        }
        call.enqueue(new Callback<PagedResult<ChatMessage>>() {

            @Override
            public void onResponse(Call<PagedResult<ChatMessage>> call, Response<PagedResult<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messages.clear();
                    PagedResult<ChatMessage> pagedMessages = response.body();
                    List<ChatMessage> msgList = pagedMessages.getItems();
                    Collections.reverse(msgList);
                    messages.addAll(msgList);
                    if(!messages.isEmpty()) {
                        lastMessage = messages.get(messages.size() - 1).getNo();
                    }
                    adapter.notifyDataSetChanged();
                    recyclerChat.scrollToPosition(messages.size() - 1);
                    startSubscriptionSSE();
                }
            }

            @Override
            public void onFailure(Call<PagedResult<ChatMessage>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startSubscriptionSSE() {
        if(shouldContinue) { // this mean there're already connection happening
             return;
        }
        shouldContinue = true;

        sseThread = new Thread(() -> {
            while (shouldContinue) {
                OkHttpClient client = getUnsafeOkHttpClient();
                HttpUrl url = HttpUrl.parse(ApiConfig.BASE_URL + "Chat/subscribe")
                        .newBuilder()
                        .addQueryParameter("messageNo", String.valueOf(lastMessage))
                        .addQueryParameter("customerId", String.valueOf(
                                isStaff ? targetedCustomerId : currentUserId))
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .header("Authorization", token)
                        .build();

                try (okhttp3.Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e("SSE", "Failed: " + response.code());
                        if (shouldContinue) {
                            Thread.sleep(3000); // Wait before retry
                        }
                        continue;
                    }

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream())
                    );
                    String line;
                    StringBuilder eventData = new StringBuilder();

                    while (shouldContinue && (line = reader.readLine()) != null) {
                        if (line.startsWith("data:")) {
                            eventData.append(line.substring(5).trim());
                            String json = eventData.toString().trim();
                            eventData.setLength(0);

                            if (!json.isEmpty()) {
                                handleIncomingMessage(json);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("SSE", "Error", e);
                    if (shouldContinue) {
                        try {
                            Thread.sleep(3000); // Wait before retry
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            Log.d("SSE", "SSE thread stopped");
        });

        sseThread.start();
    }

    private void handleIncomingMessage(String json) {
        if(json.equals("ping")){
            return;
        }
        try {
            Log.d("SSE", "Data: " + json);
            Type listType = new TypeToken<List<ChatMessage>>(){}.getType();
            List<ChatMessage> msgList = new Gson().fromJson(json, listType);
            Activity activity = getActivity();

            if (activity != null && !activity.isFinishing()) {
                activity.runOnUiThread(() -> {
                    Collections.reverse(msgList);
                    messages.addAll(msgList);
                    adapter.notifyItemRangeInserted(messages.size() - msgList.size(), msgList.size());
                    recyclerChat.smoothScrollToPosition(messages.size() - 1);
                });
            }
        } catch (Exception ex) {
            Log.e("SSE", "Failed to parse message", ex);
        }
    }

    private void stopSubscriptionSSE() {
        shouldContinue = false;
        if (sseThread != null) {
            sseThread.interrupt();
            sseThread = null;
        }
    }

    private void sendMessage(String message) {
        SendMessageRequest request = new SendMessageRequest(isStaff ?targetedCustomerId : currentUserId , message.trim());

        Call<Void> call = apiService.sendMessage(token, request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onDestroyView() {
        stopSubscriptionSSE();
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        stopSubscriptionSSE();
        super.onPause();
    }

    @Override
    public void onResume() {
        startSubscriptionSSE();
        super.onResume();
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(300, TimeUnit.SECONDS)       // for initial connection
                    .writeTimeout(15, TimeUnit.SECONDS)         // for sending requests
                    .readTimeout(0, TimeUnit.SECONDS)      // infinite read for SSE
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
