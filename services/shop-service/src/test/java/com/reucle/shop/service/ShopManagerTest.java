package com.reucle.shop.service;

import com.reucle.shop.client.IRewardClient;
import com.reucle.shop.model.Order;
import com.reucle.shop.model.ShopItem;
import com.reucle.shop.repository.IShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// PM2 Test Map: shop-service Class, Collaboration, Sequence & State Diagrams
@ExtendWith(MockitoExtension.class)
class ShopManagerTest {

    @Mock
    private IShopRepository repository;

    @Mock
    private IRewardClient rewardClient;

    private ShopManager shopManager;

    @BeforeEach
    void setUp() {
        shopManager = new ShopManager(repository, rewardClient);
    }

    @Test
    @DisplayName("[Shop Sequence: Place Order] Sufficient stock & points -> decrements stock & confirms Order")
    void placeOrder_validStockAndPoints_confirmsOrderAndDecrementsStock() {
        ShopItem voucher = new ShopItem("item-1", "Eco Tote Bag", 150, 5);
        when(repository.findItemById("item-1")).thenReturn(Optional.of(voucher));
        when(rewardClient.deductPoints(eq("u-1"), eq(300), anyString())).thenReturn(true);
        when(repository.saveItem(any(ShopItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.saveOrder(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = shopManager.placeOrder("u-1", "item-1", 2);

        assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(300, order.getTotalPoints());
        assertEquals(3, voucher.getStockQuantity());
        verify(repository).saveItem(voucher);
        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("[Shop Collaboration: Insufficient Points] Reward deduction fails -> saves CANCELLED Order without decrementing stock")
    void placeOrder_insufficientRewardPoints_cancelsOrderWithoutStockChange() {
        ShopItem voucher = new ShopItem("item-1", "Eco Tote Bag", 150, 5);
        when(repository.findItemById("item-1")).thenReturn(Optional.of(voucher));
        when(rewardClient.deductPoints(eq("u-1"), eq(150), anyString())).thenReturn(false);
        when(repository.saveOrder(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = shopManager.placeOrder("u-1", "item-1", 1);

        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
        assertEquals(5, voucher.getStockQuantity());
        verify(repository, never()).saveItem(any());
        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("[Shop State: CONFIRMED->FULFILLED] Fulfilling confirmed order -> transitions to FULFILLED")
    void fulfillOrder_confirmedOrder_transitionsToFulfilled() {
        Order order = new Order("ord-1", "u-1", "item-1", 1, 150);
        order.confirm();
        when(repository.findOrderById("ord-1")).thenReturn(Optional.of(order));
        when(repository.saveOrder(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order fulfilled = shopManager.fulfillOrder("ord-1");

        assertEquals(Order.OrderStatus.FULFILLED, fulfilled.getStatus());
        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("[Shop Sequence: Cancel Confirmed Order] Cancelling CONFIRMED order -> refunds points & restocks item")
    void cancelOrder_confirmedOrder_refundsPointsAndRestocks() {
        Order order = new Order("ord-1", "u-1", "item-1", 2, 300);
        order.confirm();
        ShopItem item = new ShopItem("item-1", "Eco Tote Bag", 150, 3);
        when(repository.findOrderById("ord-1")).thenReturn(Optional.of(order));
        when(repository.findItemById("item-1")).thenReturn(Optional.of(item));
        when(repository.saveItem(any(ShopItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.saveOrder(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order cancelled = shopManager.cancelOrder("ord-1");

        assertEquals(Order.OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals(5, item.getStockQuantity());
        verify(rewardClient).refundPoints("u-1", 300, "ord-1");
        verify(repository).saveItem(item);
        verify(repository).saveOrder(order);
    }

    @Test
    @DisplayName("[Shop State Guard] Out of stock item -> throws IllegalStateException before calling rewardClient")
    void placeOrder_outOfStock_throwsIllegalStateException() {
        ShopItem item = new ShopItem("item-2", "Metal Bottle", 500, 1);
        when(repository.findItemById("item-2")).thenReturn(Optional.of(item));

        assertThrows(IllegalStateException.class, () -> shopManager.placeOrder("u-1", "item-2", 2));
        verifyNoInteractions(rewardClient);
    }
}
