package com.example.prm392_finalproject.utils;

import android.util.Log;

import com.example.prm392_finalproject.network.ApiService;
import com.example.prm392_finalproject.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class ví dụ về cách sử dụng API
 * 
 * Hướng dẫn sử dụng:
 * 1. Tạo model class cho request và response
 * 2. Thêm endpoint vào ApiService interface
 * 3. Gọi API như ví dụ dưới đây
 */
public class ApiExample {

    private static final String TAG = "ApiExample";

    /**
     * Ví dụ cách gọi API
     */
    public void callApiExample() {
        // Tạo service instance
        ApiService apiService = RetrofitClient.createService(ApiService.class);

        // Gọi API (uncomment khi đã định nghĩa endpoint trong ApiService)
        /*
         * Call<YourResponseModel> call = apiService.getData();
         * call.enqueue(new Callback<YourResponseModel>() {
         * 
         * @Override
         * public void onResponse(Call<YourResponseModel> call,
         * Response<YourResponseModel> response) {
         * if (response.isSuccessful() && response.body() != null) {
         * YourResponseModel data = response.body();
         * Log.d(TAG, "API call successful: " + data.toString());
         * // Xử lý dữ liệu ở đây
         * } else {
         * Log.e(TAG, "API call failed with code: " + response.code());
         * }
         * }
         * 
         * @Override
         * public void onFailure(Call<YourResponseModel> call, Throwable t) {
         * Log.e(TAG, "API call failed: " + t.getMessage());
         * }
         * });
         */
    }
}
