package com.reucle.shop.model;

import java.util.Objects;

// PM2: shop-service Class & Object Diagram -> ShopItem
public class ShopItem {
    private final String itemId;
    private final String name;
    private final int pointsCost;
    private int stockQuantity;

    public ShopItem(String itemId, String name, int pointsCost, int stockQuantity) {
        this.itemId = Objects.requireNonNull(itemId);
        this.name = Objects.requireNonNull(name);
        if (pointsCost <= 0) {
            throw new IllegalArgumentException("pointsCost must be positive");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("stockQuantity cannot be negative");
        }
        this.pointsCost = pointsCost;
        this.stockQuantity = stockQuantity;
    }

    public boolean isInStock(int quantity) {
        return quantity > 0 && stockQuantity >= quantity;
    }

    // PM2: shop-service Sequence (Redeem Item) -> ShopItem.decrementStock(quantity)
    public void decrementStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > stockQuantity) {
            throw new IllegalStateException("Insufficient stock for shop item: " + itemId);
        }
        this.stockQuantity -= quantity;
    }

    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }
        this.stockQuantity += quantity;
    }

    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public int getPointsCost() { return pointsCost; }
    public int getStockQuantity() { return stockQuantity; }
}
