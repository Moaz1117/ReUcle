package com.reucle.shop.service;

import com.reucle.shop.client.IRewardClient;
import com.reucle.shop.model.Order;
import com.reucle.shop.model.ShopItem;
import com.reucle.shop.repository.IShopRepository;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

// PM2: shop-service Class, Collaboration & Sequence Diagram -> ShopManager
public class ShopManager {
    private final IShopRepository repository;
    private final IRewardClient rewardClient;

    public ShopManager(IShopRepository repository, IRewardClient rewardClient) {
        this.repository = Objects.requireNonNull(repository);
        this.rewardClient = Objects.requireNonNull(rewardClient);
    }

    // PM2: shop-service Sequence (Redeem Reward Item) -> ShopManager.placeOrder
    public Order placeOrder(String userId, String itemId, int quantity) {
        ShopItem item = repository.findItemById(itemId)
                .orElseThrow(() -> new NoSuchElementException("ShopItem not found: " + itemId));

        if (!item.isInStock(quantity)) {
            throw new IllegalStateException("Insufficient stock for item: " + itemId);
        }

        int totalPoints = item.getPointsCost() * quantity;
        Order order = new Order(UUID.randomUUID().toString(), userId, itemId, quantity, totalPoints);

        boolean pointsDeducted = rewardClient.deductPoints(userId, totalPoints, order.getOrderId());
        if (!pointsDeducted) {
            order.cancel();
            return repository.saveOrder(order);
        }

        item.decrementStock(quantity);
        repository.saveItem(item);

        order.confirm();
        return repository.saveOrder(order);
    }

    // PM2: shop-service Sequence (Fulfill Order) -> ShopManager.fulfillOrder
    public Order fulfillOrder(String orderId) {
        Order order = repository.findOrderById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
        order.fulfill();
        return repository.saveOrder(order);
    }

    // PM2: shop-service Sequence (Cancel Confirmed Order) -> ShopManager.cancelOrder
    public Order cancelOrder(String orderId) {
        Order order = repository.findOrderById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        boolean wasConfirmed = order.getStatus() == Order.OrderStatus.CONFIRMED;
        order.cancel();

        if (wasConfirmed) {
            rewardClient.refundPoints(order.getUserId(), order.getTotalPoints(), order.getOrderId());
            repository.findItemById(order.getItemId()).ifPresent(item -> {
                item.restock(order.getQuantity());
                repository.saveItem(item);
            });
        }

        return repository.saveOrder(order);
    }
}
