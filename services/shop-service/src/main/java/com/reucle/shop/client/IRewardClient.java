package com.reucle.shop.client;

// PM2: shop-service Collaboration & Sequence Diagram -> IRewardClient
public interface IRewardClient {
    boolean deductPoints(String userId, int points, String orderId);
    void refundPoints(String userId, int points, String orderId);
}
