package com.reucle.shop.model;

import java.time.Instant;
import java.util.Objects;

// PM2: shop-service Class & State Diagram -> Order
// States: PENDING -> CONFIRMED -> FULFILLED | CANCELLED
public class Order {
    public enum OrderStatus {
        PENDING, CONFIRMED, FULFILLED, CANCELLED
    }

    private final String orderId;
    private final String userId;
    private final String itemId;
    private final int quantity;
    private final int totalPoints;
    private OrderStatus status;
    private final Instant createdAt;

    public Order(String orderId, String userId, String itemId, int quantity, int totalPoints) {
        this.orderId = Objects.requireNonNull(orderId);
        this.userId = Objects.requireNonNull(userId);
        this.itemId = Objects.requireNonNull(itemId);
        if (quantity <= 0 || totalPoints <= 0) {
            throw new IllegalArgumentException("Quantity and totalPoints must be positive");
        }
        this.quantity = quantity;
        this.totalPoints = totalPoints;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
    }

    // State Transition: [PENDING] --(pointsDeducted)--> [CONFIRMED]
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm order in state: " + status);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    // State Transition: [CONFIRMED] --(deliverOrder)--> [FULFILLED]
    public void fulfill() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot fulfill order in state: " + status);
        }
        this.status = OrderStatus.FULFILLED;
    }

    // State Transition: [PENDING | CONFIRMED] --(cancelOrder)--> [CANCELLED]
    public void cancel() {
        if (this.status == OrderStatus.FULFILLED) {
            throw new IllegalStateException("Cannot cancel a FULFILLED order");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public int getTotalPoints() { return totalPoints; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
