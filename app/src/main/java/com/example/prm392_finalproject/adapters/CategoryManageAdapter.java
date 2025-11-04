package com.example.prm392_finalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.ViewHolder> {

    private Context context;
    private List<Category> categories;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEditCategory(Category category);
    }

    public CategoryManageAdapter(Context context, OnCategoryActionListener listener) {
        this.context = context;
        this.categories = new ArrayList<>();
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.tvCategoryName.setText(category.getName());
        holder.tvCategoryId.setText("ID: " + category.getId());

        // Load image with Glide
        Glide.with(context)
                .load(category.getImageUrl())
                .placeholder(R.drawable.ic_category)
                .error(R.drawable.ic_category)
                .centerCrop()
                .into(holder.ivCategoryImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditCategory(category);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditCategory(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryImage, btnEdit;
        TextView tvCategoryName, tvCategoryId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryImage = itemView.findViewById(R.id.ivCategoryImage);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryId = itemView.findViewById(R.id.tvCategoryId);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
