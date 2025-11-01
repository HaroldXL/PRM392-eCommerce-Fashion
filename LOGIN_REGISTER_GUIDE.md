# BigSize Fashion - Hướng dẫn sử dụng Login/Register

## 📱 Tổng quan

Ứng dụng shop bán hàng thời trang big size với hệ thống đăng nhập/đăng ký hiện đại.

## 🎨 Tính năng đã implement

### 1. **Login Screen** (Màn hình đăng nhập)

- ✅ UI hiện đại với gradient background
- ✅ Material Design TextInputLayout với icons
- ✅ Email validation
- ✅ Password toggle (hiện/ẩn mật khẩu)
- ✅ Loading indicator
- ✅ Forgot Password button (placeholder)
- ✅ Navigation đến Register
- ✅ Auto-login nếu đã có session

### 2. **Register Screen** (Màn hình đăng ký)

- ✅ Form đầy đủ: Full Name, Email, Phone, Address, Password, Confirm Password
- ✅ Validation cho tất cả các trường
- ✅ Password matching validation
- ✅ Auto-login sau khi đăng ký thành công
- ✅ Navigation về Login

### 3. **Main Screen** (Màn hình chính)

- ✅ Welcome message với tên user
- ✅ Toolbar với menu logout
- ✅ Session checking (tự động redirect về login nếu chưa đăng nhập)
- ✅ Logout confirmation dialog

### 4. **API Integration**

- ✅ Retrofit configuration
- ✅ SSL bypass cho localhost development
- ✅ Logging interceptor để debug
- ✅ POST /User/login endpoint
- ✅ POST /User/register endpoint

### 5. **Session Management**

- ✅ SharedPreferences để lưu token và user info
- ✅ Auto-login checking
- ✅ Logout functionality

## 🛠️ Cấu trúc Project

```
app/src/main/
├── java/com/example/prm392_finalproject/
│   ├── LoginActivity.java          # Màn hình đăng nhập
│   ├── RegisterActivity.java       # Màn hình đăng ký
│   ├── MainActivity.java           # Màn hình chính
│   ├── models/
│   │   ├── LoginRequest.java       # Model request login
│   │   ├── RegisterRequest.java    # Model request register
│   │   └── AuthResponse.java       # Model response từ API
│   ├── network/
│   │   ├── ApiConfig.java          # Cấu hình API URL
│   │   ├── ApiService.java         # Interface API endpoints
│   │   └── RetrofitClient.java     # Retrofit singleton
│   └── utils/
│       └── SessionManager.java     # Quản lý session
├── res/
│   ├── drawable/
│   │   ├── bg_gradient.xml         # Gradient background
│   │   ├── bg_button_selector.xml  # Button với effect
│   │   └── bg_edit_text.xml        # EditText styling
│   ├── layout/
│   │   ├── activity_login.xml      # Layout login
│   │   ├── activity_register.xml   # Layout register
│   │   └── activity_main.xml       # Layout main
│   ├── menu/
│   │   └── main_menu.xml           # Menu logout
│   └── values/
│       ├── colors.xml              # Màu sắc theme
│       └── strings.xml             # Các string resources
```

## 🚀 Cách sử dụng

### Bước 1: Sync Gradle

```
File → Sync Project with Gradle Files
```

### Bước 2: Cấu hình API URL

Mở file `ApiConfig.java` và chọn URL phù hợp:

```java
// Cho Android Emulator
public static final String BASE_URL = "https://10.0.2.2:7027/api/";

// Cho thiết bị thật (thay x.x bằng IP máy bạn)
public static final String BASE_URL = "https://192.168.x.x:7027/api/";
```

### Bước 3: Chạy ứng dụng

1. Build project
2. Run trên emulator hoặc thiết bị thật
3. Ứng dụng sẽ mở màn hình Login

## 🎯 Flow của ứng dụng

```
Launch App
    ↓
LoginActivity (Check session)
    ├─ Đã login → MainActivity
    └─ Chưa login → Hiển thị Login Form
         ├─ Login thành công → MainActivity
         ├─ Click "Sign Up" → RegisterActivity
         └─ Register thành công → Auto-login → MainActivity

MainActivity
    ├─ Hiển thị welcome message
    ├─ Click Logout → Confirmation Dialog
    └─ Confirm logout → LoginActivity
```

## 🎨 Màu sắc Theme

- **Primary**: `#FF6B9F` (Hồng elegant)
- **Primary Dark**: `#E5548A`
- **Accent**: `#4A90E2` (Xanh dương)
- **Background**: `#F8F9FA` (Xám nhẹ)
- **Text Primary**: `#2C3E50` (Xám đậm)

## 📝 API Endpoints

### Login

```
POST /User/login
Body: {
  "email": "user@example.com",
  "password": "string"
}
```

### Register

```
POST /User/register
Body: {
  "email": "user@example.com",
  "password": "string",
  "fullName": "string",
  "phone": "string",
  "address": "string"
}
```

## ⚠️ Lưu ý quan trọng

1. **SSL Certificate**: Code hiện tại bypass SSL validation (CHỈ dùng cho development). Production cần certificate hợp lệ.

2. **Android Emulator**: Phải dùng `10.0.2.2` thay vì `localhost`

3. **Thiết bị thật**: Cần kết nối cùng mạng WiFi với máy chạy API server

4. **Permissions**: App đã có INTERNET và ACCESS_NETWORK_STATE permissions

5. **Validation**:
   - Email: Phải đúng format
   - Password: Tối thiểu 6 ký tự
   - Phone: Tối thiểu 10 số
   - Confirm Password: Phải khớp với Password

## 🔧 Troubleshooting

### Lỗi kết nối API

- Check API server đang chạy tại `https://localhost:7027`
- Check URL trong `ApiConfig.java` đúng với môi trường (emulator/device)
- Check permissions trong AndroidManifest.xml

### UI không hiển thị đúng

- Clean & Rebuild project
- Invalidate Caches & Restart Android Studio

### Gradle sync failed

- Check internet connection
- Update Gradle version nếu cần

## 📱 Screenshots UI

### Login Screen

- Gradient background đẹp mắt
- Material Design input fields
- Smooth animations
- Clear validation messages

### Register Screen

- Form đầy đủ thông tin
- Password confirmation
- User-friendly validation

### Main Screen

- Welcome card với thông tin user
- Toolbar với logout option
- Modern design

## 🎉 Next Steps

Sau khi login/register hoàn tất, bạn có thể:

1. Thêm trang Products (danh sách sản phẩm)
2. Thêm Shopping Cart
3. Thêm Profile management
4. Thêm Order history
5. Integration với payment gateway

---

**Happy Coding! 🚀**
