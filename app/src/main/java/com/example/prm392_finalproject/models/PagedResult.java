package com.example.prm392_finalproject.models;

import java.util.ArrayList;
import java.util.List;

public class PagedResult<T> {

    private List<T> items = new ArrayList<>();
    private long totalCount;
    private int page;
    private int size;

    // --- Constructors ---
    public PagedResult() {
    }

    public PagedResult(List<T> items, long totalCount, int page, int size) {
        this.items = items;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
    }

    // --- Getters & Setters ---
    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
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

    @Override
    public String toString() {
        return "PagedResult{" +
                "items=" + items +
                ", totalCount=" + totalCount +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
