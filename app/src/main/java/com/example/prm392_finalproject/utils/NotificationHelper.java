package com.example.prm392_finalproject.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.prm392_finalproject.MainActivity;
import com.example.prm392_finalproject.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "order_notifications";
    private static final String CHANNEL_NAME = "Order Notifications";
    private static final String CHANNEL_DESC = "Notifications for order updates";
    private static final int NOTIFICATION_ID = 1001;

    /**
     * Create notification channel (required for Android 8.0+)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Show order placed notification
     */
    public static void showOrderPlacedNotification(Context context, String orderId, double totalAmount) {
        createNotificationChannel(context);

        // Create intent to open app when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("open_my_orders", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_order) // Use order icon
                .setContentTitle("Order Placed Successfully!")
                .setContentText("Your order has been placed. Total: " + formatCurrency(totalAmount))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Your order has been placed successfully!\n" +
                                "Order ID: " + orderId + "\n" +
                                "Total Amount: " + formatCurrency(totalAmount) + "\n" +
                                "Thank you for shopping with us!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Dismiss when tapped
                .setContentIntent(pendingIntent)
                .setVibrate(new long[] { 0, 500, 200, 500 }); // Vibration pattern

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            // Handle permission denial
            android.util.Log.e("NotificationHelper", "Permission denied for notification", e);
        }
    }

    /**
     * Format currency to Vietnamese Dong
     */
    private static String formatCurrency(double amount) {
        return String.format("%,.0fđ", amount);
    }
}
