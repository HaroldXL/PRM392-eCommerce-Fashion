package com.example.prm392_finalproject.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Order;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private final NumberFormat currencyFormat;
    private final SimpleDateFormat inputDateFormat;
    private final SimpleDateFormat outputDateFormat;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onCancelOrder(Order order, int position);
    }

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        this.outputDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        // Order ID
        holder.textOrderId.setText("Order #" + order.getId());

        // Status with color coding
        holder.textStatus.setText(order.getStatus());
        if ("Pending".equalsIgnoreCase(order.getStatus())) {
            holder.textStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
            holder.btnCancelOrder.setVisibility(View.VISIBLE);
        } else if ("Confirmed".equalsIgnoreCase(order.getStatus())) {
            holder.textStatus.setTextColor(Color.parseColor("#2196F3")); // Blue
            holder.btnCancelOrder.setVisibility(View.GONE);
        } else if ("Delivering".equalsIgnoreCase(order.getStatus())) {
            holder.textStatus.setTextColor(Color.parseColor("#9C27B0")); // Purple
            holder.btnCancelOrder.setVisibility(View.GONE);
        } else if ("Completed".equalsIgnoreCase(order.getStatus())) {
            holder.textStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.btnCancelOrder.setVisibility(View.GONE);
        } else if ("Cancelled".equalsIgnoreCase(order.getStatus())) {
            holder.textStatus.setTextColor(Color.parseColor("#EF4444")); // Red
            holder.btnCancelOrder.setVisibility(View.GONE);
        } else {
            holder.textStatus.setTextColor(Color.parseColor("#757575")); // Gray
            holder.btnCancelOrder.setVisibility(View.GONE);
        }

        // Date
        String dateStr = formatDate(order.getCreatedAt());
        holder.textDate.setText(dateStr);

        // Total amount
        holder.textTotal.setText(currencyFormat.format(order.getTotalAmount()));

        // Payment method
        holder.textPaymentMethod.setText(order.getPaymentMethod());

        // Items count
        int itemsCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
        holder.textItemsCount.setText(itemsCount + " item" + (itemsCount > 1 ? "s" : ""));

        // Shipping info
        holder.textShippingInfo.setText(order.getShippingAddress());

        // Cancel button click
        holder.btnCancelOrder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelOrder(order, position);
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

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        try {
            Date date = inputDateFormat.parse(dateString);
            if (date != null) {
                return outputDateFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateString;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderId;
        TextView textStatus;
        TextView textDate;
        TextView textTotal;
        TextView textPaymentMethod;
        TextView textItemsCount;
        TextView textShippingInfo;
        MaterialButton btnCancelOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textStatus = itemView.findViewById(R.id.textStatus);
            textDate = itemView.findViewById(R.id.textDate);
            textTotal = itemView.findViewById(R.id.textTotal);
            textPaymentMethod = itemView.findViewById(R.id.textPaymentMethod);
            textItemsCount = itemView.findViewById(R.id.textItemsCount);
            textShippingInfo = itemView.findViewById(R.id.textShippingInfo);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
        }
    }
}
