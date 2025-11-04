package com.example.prm392_finalproject.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.prm392_finalproject.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {

    private static final String TAG = "CloudinaryHelper";

    private static boolean isInitialized = false;

    /**
     * Initialize Cloudinary MediaManager
     */
    public static void init(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
            config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
            config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);
            config.put("secure", "true");

            MediaManager.init(context, config);
            isInitialized = true;
            Log.d(TAG, "Cloudinary initialized successfully");
        }
    }

    /**
     * Upload image from URI
     */
    public static void uploadImage(Context context, Uri imageUri, final CloudinaryUploadCallback callback) {
        try {
            // Convert Uri to File
            File imageFile = getFileFromUri(context, imageUri);

            if (imageFile == null) {
                callback.onError("Failed to read image file");
                return;
            }

            // Upload options
            Map<String, Object> options = new HashMap<>();
            options.put("folder", "ecommerce_users"); // Folder trong Cloudinary
            options.put("resource_type", "image");

            // Upload
            MediaManager.get().upload(imageFile.getAbsolutePath())
                    .option("folder", "ecommerce_users")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "Upload started: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            int progress = (int) ((bytes * 100) / totalBytes);
                            Log.d(TAG, "Upload progress: " + progress + "%");
                            callback.onProgress(progress);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");
                            Log.d(TAG, "Upload success: " + imageUrl);
                            callback.onSuccess(imageUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "Upload error: " + error.getDescription());
                            callback.onError(error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                        }
                    })
                    .dispatch();

        } catch (Exception e) {
            Log.e(TAG, "Upload exception: " + e.getMessage(), e);
            callback.onError(e.getMessage());
        }
    }

    /**
     * Convert Uri to File
     */
    private static File getFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null)
                return null;

            File tempFile = new File(context.getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Error converting Uri to File: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Callback interface for upload
     */
    public interface CloudinaryUploadCallback {
        void onSuccess(String imageUrl);

        void onError(String error);

        void onProgress(int progress);
    }
}
