package com.example.prm392_finalproject.network;

public class ApiConfig {
    // Base URL cho API
    // Lưu ý:
    // - Dùng "https://10.0.2.2:7027/api/" cho Android Emulator
    // - Dùng "https://192.168.x.x:7027/api/" cho thiết bị thật (thay x.x bằng IP
    // máy bạn)
    // - Dùng "https://localhost:7027/api/" nếu test trên máy local
    public static final String BASE_URL = "https://10.0.2.2:7027/api/";

    // Timeout settings (in seconds)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
}
