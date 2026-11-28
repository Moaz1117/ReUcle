package com.reucle.inventory.service;

import com.reucle.inventory.model.InventoryItem;
import com.reucle.inventory.repository.IInventoryRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

// PM2: inventory-service Class/Collaboration/Sequence Diagram -> InventoryManager
public class InventoryManager {
    private final IInventoryRepository repository;

    public InventoryManager(IInventoryRepository repository) {
        this.repository = repository;
    }

    // PM2: inventory-service Sequence (Record Deposit) -> InventoryManager.recordDeposit
    public InventoryItem recordDeposit(String binId, String materialId, double weightKg) {
        InventoryItem item = repository.findByBinIdAndMaterialId(binId, materialId)
                .map(existing -> {
                    existing.addStock(weightKg);
                    return existing;
                })
                .orElseGet(() -> new InventoryItem(UUID.randomUUID().toString(), binId, materialId, weightKg));
        return repository.save(item);
    }

    // PM2: inventory-service Sequence (Dispatch Stock) -> InventoryManager.dispatchStock
    public InventoryItem dispatchStock(String itemId, double weightKg) {
        InventoryItem item = repository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Inventory item not found: " + itemId));
        item.consumeStock(weightKg);
        return repository.save(item);
    }

    public List<InventoryItem> getAllItems() {
        return repository.findAll();
    }
}
