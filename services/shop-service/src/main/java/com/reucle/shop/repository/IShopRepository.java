package com.reucle.shop.repository;

import com.reucle.shop.model.Order;
import com.reucle.shop.model.ShopItem;
import java.util.List;
import java.util.Optional;

// PM2: shop-service Class & Collaboration Diagram -> IShopRepository
public interface IShopRepository {
    Optional<ShopItem> findItemById(String itemId);
    List<ShopItem> findAllItems();
    ShopItem saveItem(ShopItem item);
    Optional<Order> findOrderById(String orderId);
    Order saveOrder(Order order);
}
