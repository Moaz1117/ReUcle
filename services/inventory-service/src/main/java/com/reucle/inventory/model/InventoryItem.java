package com.reucle.inventory.model;

import java.util.Objects;

// PM2: inventory-service Class & State Diagram -> InventoryItem
// States: AVAILABLE <-> RESERVED; AVAILABLE -> DEPLETED
public class InventoryItem {
    public enum InventoryStatus {
        AVAILABLE, RESERVED, DEPLETED
    }

    private final String itemId;
    private final String binId;
    private final String materialId;
    private double quantityKg;
    private InventoryStatus status;

    public InventoryItem(String itemId, String binId, String materialId, double quantityKg) {
        this.itemId = Objects.requireNonNull(itemId);
        this.binId = Objects.requireNonNull(binId);
        this.materialId = Objects.requireNonNull(materialId);
        if (quantityKg < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantityKg = quantityKg;
        this.status = quantityKg > 0 ? InventoryStatus.AVAILABLE : InventoryStatus.DEPLETED;
    }

    // PM2: inventory-service Sequence/Collaboration -> InventoryItem.addStock(deltaKg)
    public void addStock(double deltaKg) {
        if (deltaKg <= 0) {
            throw new IllegalArgumentException("Added quantity must be positive");
        }
        this.quantityKg += deltaKg;
        if (this.status == InventoryStatus.DEPLETED && this.quantityKg > 0) {
            this.status = InventoryStatus.AVAILABLE;
        }
    }

    // PM2: inventory-service Sequence/Collaboration -> InventoryItem.consumeStock(amountKg)
    public void consumeStock(double amountKg) {
        if (amountKg <= 0) {
            throw new IllegalArgumentException("Consumed quantity must be positive");
        }
        if (amountKg > this.quantityKg) {
            throw new IllegalStateException("Insufficient stock for item: " + itemId);
        }
        this.quantityKg -= amountKg;
        if (this.quantityKg == 0) {
            this.status = InventoryStatus.DEPLETED;
        }
    }

    public String getItemId() { return itemId; }
    public String getBinId() { return binId; }
    public String getMaterialId() { return materialId; }
    public double getQuantityKg() { return quantityKg; }
    public InventoryStatus getStatus() { return status; }
}
