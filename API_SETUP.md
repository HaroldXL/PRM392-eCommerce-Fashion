# Hướng dẫn cấu hình và sử dụng API

## Base URL đã được cấu hình

```
https://localhost:7027/api/
```

## Các file đã được tạo

### 1. `network/ApiConfig.java`

- Chứa cấu hình base URL và timeout settings
- Có thể thay đổi URL tại đây nếu cần

### 2. `network/RetrofitClient.java`

- Tạo và quản lý Retrofit instance
- Đã cấu hình bypass SSL cho localhost (CHỈ dùng cho development)
- Có logging interceptor để debug

### 3. `network/ApiService.java`

- Interface định nghĩa các API endpoints
- Thêm các methods API của bạn vào đây

### 4. `utils/ApiExample.java`

- Ví dụ về cách gọi API

## Cách sử dụng

### Bước 1: Tạo Model Classes

```java
// Example Response Model
public class UserResponse {
    private int id;
    private String name;
    private String email;

    // Getters and setters
}

// Example Request Model
public class LoginRequest {
    private String username;
    private String password;

    // Constructor, getters and setters
}
```

### Bước 2: Định nghĩa API trong ApiService

```java
public interface ApiService {
    @GET("users")
    Call<List<UserResponse>> getUsers();

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("users/{id}")
    Call<UserResponse> getUserById(@Path("id") int id);
}
```

### Bước 3: Gọi API trong Activity/Fragment

```java
ApiService apiService = RetrofitClient.createService(ApiService.class);

Call<List<UserResponse>> call = apiService.getUsers();
call.enqueue(new Callback<List<UserResponse>>() {
    @Override
    public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
        if (response.isSuccessful() && response.body() != null) {
            List<UserResponse> users = response.body();
            // Xử lý dữ liệu
        }
    }

    @Override
    public void onFailure(Call<List<UserResponse>> call, Throwable t) {
        // Xử lý lỗi
        Log.e("API", "Error: " + t.getMessage());
    }
});
```

## Lưu ý quan trọng

### Cho Android Emulator kết nối localhost

Nếu bạn chạy API trên máy local và test trên emulator:

- Thay `localhost` bằng `10.0.2.2` (special alias cho host machine của emulator)
- Hoặc dùng IP thực của máy (VD: 192.168.x.x)

Ví dụ trong `ApiConfig.java`:

```java
public static final String BASE_URL = "https://10.0.2.2:7027/api/";
```

### Bảo mật

- Code hiện tại bypass SSL certificate validation (chỉ dùng cho development)
- Trong production, cần sử dụng certificate hợp lệ hoặc thêm certificate vào trust store

### Dependencies đã thêm

- Retrofit 2.9.0
- Gson Converter 2.9.0
- OkHttp Logging Interceptor 4.11.0

## Sync Project

Sau khi thêm dependencies, hãy sync Gradle:

- Click "Sync Now" trong Android Studio
- Hoặc: File → Sync Project with Gradle Files
