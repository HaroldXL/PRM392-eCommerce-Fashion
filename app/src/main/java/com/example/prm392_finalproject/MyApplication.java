package com.example.prm392_finalproject;

import android.app.Application;

import com.example.prm392_finalproject.utils.CloudinaryHelper;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Cloudinary
        CloudinaryHelper.init(this);
    }
}
