package com.reucle.inventory.service;

import com.reucle.inventory.model.InventoryItem;
import com.reucle.inventory.repository.IInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// PM2 Test Map: inventory-service Class, Sequence & State Diagrams
@ExtendWith(MockitoExtension.class)
class InventoryManagerTest {

    @Mock
    private IInventoryRepository repository;

    private InventoryManager manager;

    @BeforeEach
    void setUp() {
        manager = new InventoryManager(repository);
    }

    @Test
    @DisplayName("[Inventory Sequence: Record Deposit] Existing bin/material -> increments quantityKg")
    void recordDeposit_existingItem_incrementsQuantity() {
        InventoryItem existing = new InventoryItem("item-1", "bin-1", "mat-plastic", 10.0);
        when(repository.findByBinIdAndMaterialId("bin-1", "mat-plastic")).thenReturn(Optional.of(existing));
        when(repository.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryItem updated = manager.recordDeposit("bin-1", "mat-plastic", 5.5);

        assertEquals(15.5, updated.getQuantityKg());
        assertEquals(InventoryItem.InventoryStatus.AVAILABLE, updated.getStatus());
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("[Inventory Sequence: Record Deposit] New bin/material -> creates AVAILABLE InventoryItem")
    void recordDeposit_newItem_createsAvailableItem() {
        when(repository.findByBinIdAndMaterialId("bin-2", "mat-glass")).thenReturn(Optional.empty());
        when(repository.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryItem created = manager.recordDeposit("bin-2", "mat-glass", 8.0);

        assertEquals("bin-2", created.getBinId());
        assertEquals(8.0, created.getQuantityKg());
        assertEquals(InventoryItem.InventoryStatus.AVAILABLE, created.getStatus());
        verify(repository).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("[Inventory State: AVAILABLE->DEPLETED] Dispatching full stock -> transitions to DEPLETED")
    void dispatchStock_fullAmount_transitionsToDepleted() {
        InventoryItem item = new InventoryItem("item-1", "bin-1", "mat-plastic", 10.0);
        when(repository.findById("item-1")).thenReturn(Optional.of(item));
        when(repository.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryItem result = manager.dispatchStock("item-1", 10.0);

        assertEquals(0.0, result.getQuantityKg());
        assertEquals(InventoryItem.InventoryStatus.DEPLETED, result.getStatus());
        verify(repository).save(item);
    }

    @Test
    @DisplayName("[Inventory Guard] Dispatching above available stock -> throws IllegalStateException")
    void dispatchStock_insufficientStock_throwsIllegalStateException() {
        InventoryItem item = new InventoryItem("item-1", "bin-1", "mat-plastic", 4.0);
        when(repository.findById("item-1")).thenReturn(Optional.of(item));

        assertThrows(IllegalStateException.class, () -> manager.dispatchStock("item-1", 6.0));
        verify(repository, never()).save(any());
    }
}
