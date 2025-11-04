package com.example.prm392_finalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<BannerItem> bannerItems;

    public BannerAdapter() {
        this.bannerItems = new ArrayList<>();
        // Add banners with images
        bannerItems.add(
                new BannerItem("24% off shipping today\non bag purchases", "By Kukuku Store", R.drawable.banner_1));
        bannerItems.add(new BannerItem("New Arrivals\nSpring Collection", "Fashion Store", R.drawable.banner_2));
        bannerItems.add(new BannerItem("Special Discount\nUp to 50% OFF", "BigSize Fashion", R.drawable.banner_3));
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem item = bannerItems.get(position);
        holder.tvBannerTitle.setText(item.getTitle());
        holder.tvBannerSubtitle.setText(item.getSubtitle());
        holder.ivBannerImage.setImageResource(item.getImageResId());
    }

    @Override
    public int getItemCount() {
        return bannerItems.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        TextView tvBannerTitle;
        TextView tvBannerSubtitle;
        ImageView ivBannerImage;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBannerTitle = itemView.findViewById(R.id.tvBannerTitle);
            tvBannerSubtitle = itemView.findViewById(R.id.tvBannerSubtitle);
            ivBannerImage = itemView.findViewById(R.id.ivBannerImage);
        }
    }

    public static class BannerItem {
        private String title;
        private String subtitle;
        private int imageResId;

        public BannerItem(String title, String subtitle, int imageResId) {
            this.title = title;
            this.subtitle = subtitle;
            this.imageResId = imageResId;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public int getImageResId() {
            return imageResId;
        }
    }
}
