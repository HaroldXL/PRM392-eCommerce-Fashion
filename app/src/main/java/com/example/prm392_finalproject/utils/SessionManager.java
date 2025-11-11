package com.example.prm392_finalproject.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String PREF_NAME = "BigSizeFashionPref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ROLE = "role";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(String token, String email, String fullName, int role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putInt(KEY_ROLE, role);
        editor.commit();
    }

    public void saveUserId(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    public int getRole() {
        return prefs.getInt(KEY_ROLE, -1);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, 0);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }
}
