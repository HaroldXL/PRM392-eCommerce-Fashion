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
import com.example.prm392_finalproject.models.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductManageAdapter extends RecyclerView.Adapter<ProductManageAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> products = new ArrayList<>();
    private OnProductActionListener listener;
    private NumberFormat currencyFormat;

    public interface OnProductActionListener {
        void onEditProduct(Product product);
    }

    public ProductManageAdapter(Context context, OnProductActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void setProducts(List<Product> products) {
        this.products = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_manage, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvCategoryName
                .setText("Category: " + (product.getCategoryName() != null ? product.getCategoryName() : "N/A"));
        holder.tvPrice.setText(currencyFormat.format(product.getPrice()));

        // Show variants count and total stock
        if (product.getProductVariants() != null && !product.getProductVariants().isEmpty()) {
            int totalStock = 0;
            for (com.example.prm392_finalproject.models.ProductVariant variant : product.getProductVariants()) {
                totalStock += variant.getStockQuantity();
            }
            holder.tvVariantsCount.setText(product.getProductVariants().size() + " variants • Stock: " + totalStock);
        } else {
            holder.tvVariantsCount.setText("No variants");
        }

        // Load product image
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_category)
                .error(R.drawable.ic_category)
                .centerCrop()
                .into(holder.ivProductImage);

        // Click on entire card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct(product);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, btnEdit;
        TextView tvProductName, tvCategoryName, tvPrice, tvVariantsCount;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvVariantsCount = itemView.findViewById(R.id.tvVariantsCount);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
