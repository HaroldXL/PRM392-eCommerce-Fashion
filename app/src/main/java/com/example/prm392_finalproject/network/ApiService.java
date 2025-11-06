package com.example.prm392_finalproject.network;

import com.example.prm392_finalproject.models.AuthResponse;
import com.example.prm392_finalproject.models.Category;
import com.example.prm392_finalproject.models.CreateCategoryRequest;
import com.example.prm392_finalproject.models.CreateOrderRequest;
import com.example.prm392_finalproject.models.CreateProductRequest;
import com.example.prm392_finalproject.models.ForgotPasswordRequest;
import com.example.prm392_finalproject.models.LoginRequest;
import com.example.prm392_finalproject.models.MessageResponse;
import com.example.prm392_finalproject.models.Order;
import com.example.prm392_finalproject.models.PaymentInitRequest;
import com.example.prm392_finalproject.models.PaymentInitResponse;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.models.ProductDetail;
import com.example.prm392_finalproject.models.ProductListResponse;
import com.example.prm392_finalproject.models.ProductResponse;
import com.example.prm392_finalproject.models.RegisterRequest;
import com.example.prm392_finalproject.models.ResetPasswordRequest;
import com.example.prm392_finalproject.models.UpdateCategoryRequest;
import com.example.prm392_finalproject.models.UpdateProductRequest;
import com.example.prm392_finalproject.models.User;
import com.example.prm392_finalproject.models.UserResponse;
import com.example.prm392_finalproject.models.UpdateProfileRequest;
import com.example.prm392_finalproject.models.UpdateUserRequest;
import com.example.prm392_finalproject.models.VerifyOtpRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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
        Call<ProductListResponse> getProducts(
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("filter_by_name") String filterByName,
                        @Query("category_id") Integer categoryId);

        @GET("Product/{id}")
        Call<Product> getProductById(@Path("id") int productId);

        @POST("Product")
        Call<Product> createProduct(@Header("Authorization") String token, @Body CreateProductRequest request);

        @PATCH("Product/{id}")
        Call<Product> updateProduct(@Path("id") int productId, @Header("Authorization") String token,
                        @Body UpdateProductRequest request);

        @GET("Product/{id}")
        Call<ProductDetail> getProductDetail(@Path("id") int productId);

        @GET("Category")
        Call<List<Category>> getCategories();

        @GET("Category/{id}")
        Call<Category> getCategoryById(@Path("id") int categoryId, @Header("Authorization") String token);

        @POST("Category")
        Call<Category> createCategory(@Header("Authorization") String token, @Body CreateCategoryRequest request);

        @PATCH("Category/{id}")
        Call<Category> updateCategory(@Path("id") int categoryId, @Header("Authorization") String token,
                        @Body UpdateCategoryRequest request);

        @GET("User/me")
        Call<User> getUserProfile(@Header("Authorization") String token);

        @GET("User")
        Call<UserResponse> getUsers(
                        @Header("Authorization") String token,
                        @Query("Email") String email,
                        @Query("FullName") String fullName,
                        @Query("Phone") String phone,
                        @Query("Role") Integer role,
                        @Query("Page") int page,
                        @Query("Size") int size);

        @GET("User/{id}")
        Call<User> getUserById(@Path("id") int userId, @Header("Authorization") String token);

        @DELETE("User/{id}")
        Call<Void> deleteUser(@Path("id") int userId, @Header("Authorization") String token);

        @PATCH("User/{id}")
        Call<User> updateUser(@Path("id") int userId, @Header("Authorization") String token,
                        @Body UpdateUserRequest request);

        @PATCH("User/me")
        Call<User> updateUserProfile(@Header("Authorization") String token, @Body UpdateProfileRequest request);

        @POST("User/forgot-password")
        Call<MessageResponse> forgotPassword(@Body ForgotPasswordRequest request);

        @POST("User/verify-otp")
        Call<MessageResponse> verifyOtp(@Body VerifyOtpRequest request);

        @POST("User/reset-password")
        Call<MessageResponse> resetPassword(@Body ResetPasswordRequest request);

        // Order endpoints
        @GET("Order")
        Call<List<Order>> getOrders(@Header("Authorization") String token);

        @POST("Order")
        Call<MessageResponse> createOrder(@Header("Authorization") String token, @Body CreateOrderRequest request);

        // Payment endpoints
        @POST("Payment/init")
        Call<PaymentInitResponse> initPayment(@Header("Authorization") String token, @Body PaymentInitRequest request);
}
