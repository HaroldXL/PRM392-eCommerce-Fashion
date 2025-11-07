package com.example.prm392_finalproject.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Order;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private OnOrderActionListener listener;
    private final NumberFormat currencyFormat;

    public interface OnOrderActionListener {
        void onOrderAction(String action, Order order);
    }

    public AdminOrderAdapter(List<Order> orders, OnOrderActionListener listener) {
        this.orders = orders;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        // Order ID
        holder.textOrderId.setText("Order #" + order.getId());

        // User ID
        holder.textUserId.setText("User ID: " + order.getUserId());

        // Status with color coding and rounded background
        holder.textStatus.setText(order.getStatus());
        String statusBgColor, statusTextColor;

        if ("Pending".equalsIgnoreCase(order.getStatus())) {
            statusTextColor = "#FF9800"; // Orange text
            statusBgColor = "#FFF3E0"; // Light orange background
        } else if ("Confirmed".equalsIgnoreCase(order.getStatus())) {
            statusTextColor = "#2196F3"; // Blue text
            statusBgColor = "#E3F2FD"; // Light blue background
        } else if ("Delivering".equalsIgnoreCase(order.getStatus())) {
            statusTextColor = "#9C27B0"; // Purple text
            statusBgColor = "#F3E5F5"; // Light purple background
        } else if ("Completed".equalsIgnoreCase(order.getStatus())) {
            statusTextColor = "#4CAF50"; // Green text
            statusBgColor = "#E8F5E9"; // Light green background
        } else {
            statusTextColor = "#757575"; // Gray text
            statusBgColor = "#F5F5F5"; // Light gray background
        }

        holder.textStatus.setTextColor(Color.parseColor(statusTextColor));

        // Create rounded background drawable
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.parseColor(statusBgColor));
        drawable.setCornerRadius(12f); // 12dp corner radius
        holder.textStatus.setBackground(drawable);

        // Payment Status
        holder.textPaymentStatus.setText(order.getPaymentStatus());
        if ("Paid".equalsIgnoreCase(order.getPaymentStatus())) {
            holder.textPaymentStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.textPaymentStatus.setTextColor(Color.parseColor("#FF5722")); // Red
        }

        // Date
        String dateStr = formatDate(order.getCreatedAt());
        holder.textDate.setText(dateStr);

        // Total amount
        holder.textTotal.setText(currencyFormat.format(order.getTotalAmount()) + "đ");

        // Payment method
        holder.textPaymentMethod.setText(order.getPaymentMethod());

        // Items count
        int itemsCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
        holder.textItemsCount.setText(itemsCount + " item" + (itemsCount > 1 ? "s" : ""));

        // Shipping info
        holder.textShippingInfo.setText(order.getShippingAddress());
        holder.textCustomerName.setText(order.getShippingName());
        holder.textCustomerPhone.setText(order.getShippingPhone());

        // Update Status Button
        if ("Completed".equalsIgnoreCase(order.getStatus())) {
            holder.btnUpdateStatus.setEnabled(false);
            holder.btnUpdateStatus.setText("Completed");
            holder.btnUpdateStatus.setAlpha(0.5f);
        } else {
            holder.btnUpdateStatus.setEnabled(true);
            holder.btnUpdateStatus.setText("Next Status");
            holder.btnUpdateStatus.setAlpha(1.0f);
            holder.btnUpdateStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderAction("UPDATE_STATUS", order);
                }
            });
        }

        // Delete Button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderAction("DELETE", order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            Date date = inputFormat.parse(isoDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return isoDate;
        }
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderId, textUserId, textStatus, textPaymentStatus, textDate, textTotal;
        TextView textPaymentMethod, textItemsCount, textShippingInfo;
        TextView textCustomerName, textCustomerPhone;
        Button btnUpdateStatus, btnDelete;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textUserId = itemView.findViewById(R.id.textUserId);
            textStatus = itemView.findViewById(R.id.textStatus);
            textPaymentStatus = itemView.findViewById(R.id.textPaymentStatus);
            textDate = itemView.findViewById(R.id.textDate);
            textTotal = itemView.findViewById(R.id.textTotal);
            textPaymentMethod = itemView.findViewById(R.id.textPaymentMethod);
            textItemsCount = itemView.findViewById(R.id.textItemsCount);
            textShippingInfo = itemView.findViewById(R.id.textShippingInfo);
            textCustomerName = itemView.findViewById(R.id.textCustomerName);
            textCustomerPhone = itemView.findViewById(R.id.textCustomerPhone);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
