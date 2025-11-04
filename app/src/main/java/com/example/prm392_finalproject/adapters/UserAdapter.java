package com.example.prm392_finalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.User;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> users;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onViewUser(User user);

        void onDeleteUser(User user);
    }

    public UserAdapter(Context context, OnUserActionListener listener) {
        this.context = context;
        this.users = new ArrayList<>();
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        android.util.Log.d("UserAdapter", "setUsers called - Size: " + (users != null ? users.size() : "null"));
        notifyDataSetChanged();
    }

    public void addUsers(List<User> newUsers) {
        int startPosition = this.users.size();
        this.users.addAll(newUsers);
        android.util.Log.d("UserAdapter",
                "addUsers called - New items: " + newUsers.size() + ", Total: " + this.users.size());
        notifyItemRangeInserted(startPosition, newUsers.size());
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        android.util.Log.d("UserAdapter", "Binding user at position " + position + ": " + user.getFullName());

        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhone());
        holder.tvRole.setText(user.getRoleName());

        // Set role badge color
        int roleColor;
        switch (user.getRole()) {
            case 1: // Admin
                roleColor = 0xFFEF4444; // Red
                break;
            case 3: // Manager
                roleColor = 0xFFF59E0B; // Orange
                break;
            default: // Customer
                roleColor = 0xFF10B981; // Green
                break;
        }
        holder.tvRole.setBackgroundColor(roleColor);

        // Get first letter of name for avatar
        String firstLetter = user.getFullName().isEmpty() ? "U" : user.getFullName().substring(0, 1).toUpperCase();
        holder.tvAvatar.setText(firstLetter);

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewUser(user);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteUser(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = users.size();
        android.util.Log.d("UserAdapter", "getItemCount: " + count);
        return count;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvAvatar, tvName, tvEmail, tvPhone, tvRole;
        ImageView btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
