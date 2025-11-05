package com.example.prm392_finalproject.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.models.ProductVariant;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class VariantEditAdapter extends RecyclerView.Adapter<VariantEditAdapter.VariantViewHolder> {

    private Context context;
    private List<ProductVariant> variants = new ArrayList<>();

    public VariantEditAdapter(Context context) {
        this.context = context;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants != null ? variants : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public void addVariant() {
        variants.add(new ProductVariant("", "", 0));
        notifyItemInserted(variants.size() - 1);
    }

    public void removeVariant(int position) {
        if (position >= 0 && position < variants.size()) {
            variants.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public VariantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_variant_edit, parent, false);
        return new VariantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VariantViewHolder holder, int position) {
        ProductVariant variant = variants.get(position);

        holder.clearWatchers();

        holder.etSize.setText(variant.getSize());
        holder.etColor.setText(variant.getColor());
        holder.etStock.setText(variant.getStockQuantity() > 0 ? String.valueOf(variant.getStockQuantity()) : "");

        holder.setupWatchers(variant);

        holder.btnDelete.setOnClickListener(v -> removeVariant(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return variants.size();
    }

    class VariantViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText etSize, etColor, etStock;
        ImageView btnDelete;

        private TextWatcher sizeWatcher, colorWatcher, stockWatcher;

        public VariantViewHolder(@NonNull View itemView) {
            super(itemView);
            etSize = itemView.findViewById(R.id.etSize);
            etColor = itemView.findViewById(R.id.etColor);
            etStock = itemView.findViewById(R.id.etStock);
            btnDelete = itemView.findViewById(R.id.btnDeleteVariant);
        }

        void clearWatchers() {
            if (sizeWatcher != null)
                etSize.removeTextChangedListener(sizeWatcher);
            if (colorWatcher != null)
                etColor.removeTextChangedListener(colorWatcher);
            if (stockWatcher != null)
                etStock.removeTextChangedListener(stockWatcher);
        }

        void setupWatchers(ProductVariant variant) {
            sizeWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    variant.setSize(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };

            colorWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    variant.setColor(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };

            stockWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        variant.setStockQuantity(s.length() > 0 ? Integer.parseInt(s.toString()) : 0);
                    } catch (NumberFormatException e) {
                        variant.setStockQuantity(0);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };

            etSize.addTextChangedListener(sizeWatcher);
            etColor.addTextChangedListener(colorWatcher);
            etStock.addTextChangedListener(stockWatcher);
        }
    }
}
