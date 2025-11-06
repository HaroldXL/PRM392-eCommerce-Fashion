package com.example.prm392_finalproject.utils;

import android.app.Activity;
import android.util.Log;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class ZaloPayHelper {
    private static final String TAG = "ZaloPayHelper";

    // ZaloPay Sandbox credentials (Demo)
    private static final String SANDBOX_APP_ID = "2554";

    /**
     * Initialize ZaloPay SDK with Sandbox environment
     */
    public static void init(Activity activity) {
        ZaloPaySDK.init(Integer.parseInt(SANDBOX_APP_ID), Environment.SANDBOX);
        Log.d(TAG, "ZaloPay SDK initialized with Sandbox environment");
    }

    /**
     * Pay with ZaloPay using zpTransToken from backend
     * 
     * @param activity     Current activity
     * @param zpTransToken Token received from backend API
     * @param listener     Payment result listener
     */
    public static void payOrder(Activity activity, String zpTransToken, PayOrderListener listener) {
        ZaloPaySDK.getInstance().payOrder(activity, zpTransToken, "demozpdk://app", listener);
    }
}
