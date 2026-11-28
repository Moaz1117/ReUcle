package com.reucle.inventory.repository;

import com.reucle.inventory.model.InventoryItem;
import java.util.List;
import java.util.Optional;

// PM2: inventory-service Class/Collaboration Diagram -> IInventoryRepository
public interface IInventoryRepository {
    Optional<InventoryItem> findById(String itemId);
    Optional<InventoryItem> findByBinIdAndMaterialId(String binId, String materialId);
    List<InventoryItem> findAll();
    InventoryItem save(InventoryItem item);
}
