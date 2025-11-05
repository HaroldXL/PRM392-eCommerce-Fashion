package com.example.prm392_finalproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.prm392_finalproject.models.CartItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREF_NAME = "CartPreferences";
    private static final String KEY_CART_ITEMS = "cart_items";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public CartManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<CartItem> getCartItems() {
        String json = sharedPreferences.getString(KEY_CART_ITEMS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<CartItem>>() {
        }.getType();
        List<CartItem> items = gson.fromJson(json, type);

        // Clean invalid items (productVariantId = 0) - old data before migration
        List<CartItem> validItems = new ArrayList<>();
        boolean hasInvalidItems = false;
        for (CartItem item : items) {
            if (item.getProductVariantId() > 0) {
                validItems.add(item);
            } else {
                hasInvalidItems = true;
            }
        }

        // Save cleaned cart if we removed invalid items
        if (hasInvalidItems) {
            saveCart(validItems);
        }

        return validItems;
    }

    public void addToCart(CartItem item) {
        List<CartItem> cartItems = getCartItems();

        // Check if product variant already exists in cart
        boolean found = false;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductVariantId() == item.getProductVariantId()) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                found = true;
                break;
            }
        }

        if (!found) {
            cartItems.add(item);
        }

        saveCart(cartItems);
    }

    public void updateQuantity(int productVariantId, int quantity) {
        List<CartItem> cartItems = getCartItems();
        for (CartItem item : cartItems) {
            if (item.getProductVariantId() == productVariantId) {
                item.setQuantity(quantity);
                break;
            }
        }
        saveCart(cartItems);
    }

    public void removeFromCart(int productVariantId) {
        List<CartItem> cartItems = getCartItems();
        cartItems.removeIf(item -> item.getProductVariantId() == productVariantId);
        saveCart(cartItems);
    }

    public void clearCart() {
        sharedPreferences.edit().remove(KEY_CART_ITEMS).apply();
    }

    public int getCartItemCount() {
        return getCartItems().size();
    }

    public double getTotalPrice() {
        List<CartItem> cartItems = getCartItems();
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    private void saveCart(List<CartItem> cartItems) {
        String json = gson.toJson(cartItems);
        sharedPreferences.edit().putString(KEY_CART_ITEMS, json).apply();
    }
}
