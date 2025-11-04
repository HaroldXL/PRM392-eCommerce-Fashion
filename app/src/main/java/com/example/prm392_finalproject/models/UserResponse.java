package com.example.prm392_finalproject.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserResponse {
    private List<User> items;

    @SerializedName("totalCount")
    private int totalCount;

    private int page;
    private int size;

    public List<User> getItems() {
        return items;
    }

    public void setItems(List<User> items) {
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
