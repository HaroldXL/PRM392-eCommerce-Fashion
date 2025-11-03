package com.example.prm392_finalproject.network;

import com.example.prm392_finalproject.models.AuthResponse;
import com.example.prm392_finalproject.models.Category;
import com.example.prm392_finalproject.models.CreateOrderRequest;
import com.example.prm392_finalproject.models.ForgotPasswordRequest;
import com.example.prm392_finalproject.models.LoginRequest;
import com.example.prm392_finalproject.models.MessageResponse;
import com.example.prm392_finalproject.models.ProductDetail;
import com.example.prm392_finalproject.models.ProductResponse;
import com.example.prm392_finalproject.models.RegisterRequest;
import com.example.prm392_finalproject.models.ResetPasswordRequest;
import com.example.prm392_finalproject.models.User;
import com.example.prm392_finalproject.models.UpdateProfileRequest;
import com.example.prm392_finalproject.models.VerifyOtpRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface định nghĩa các API endpoints
 */
public interface ApiService {

    @POST("User/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("User/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("Product")
    Call<ProductResponse> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("filter_by_name") String filterByName,
            @Query("category_id") Integer categoryId);

    @GET("Product/{id}")
    Call<ProductDetail> getProductDetail(@Path("id") int productId);

    @GET("Category")
    Call<List<Category>> getCategories();

    @GET("User/me")
    Call<User> getUserProfile(@Header("Authorization") String token);

    @PATCH("User/me")
    Call<User> updateUserProfile(@Header("Authorization") String token, @Body UpdateProfileRequest request);

    @POST("User/forgot-password")
    Call<MessageResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("User/verify-otp")
    Call<MessageResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("User/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest request);

    @POST("Order")
    Call<MessageResponse> createOrder(@Header("Authorization") String token, @Body CreateOrderRequest request);
}
