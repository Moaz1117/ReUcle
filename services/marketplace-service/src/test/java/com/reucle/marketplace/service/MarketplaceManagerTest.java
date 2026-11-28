package com.reucle.marketplace.service;

import com.reucle.marketplace.model.Listing;
import com.reucle.marketplace.repository.IListingRepository;
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

// PM2 Test Map: marketplace-service Class, Sequence & State Diagrams
@ExtendWith(MockitoExtension.class)
class MarketplaceManagerTest {

    @Mock
    private IListingRepository repository;

    private MarketplaceManager manager;

    @BeforeEach
    void setUp() {
        manager = new MarketplaceManager(repository);
    }

    @Test
    @DisplayName("[Marketplace Sequence: Create] Valid batch -> creates ACTIVE listing with computed totalPrice")
    void createListing_validInput_createsActiveListing() {
        when(repository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        Listing listing = manager.createListing("seller-1", "mat-alu", 20.0, 15.0);

        assertEquals(Listing.ListingStatus.ACTIVE, listing.getStatus());
        assertEquals(300.0, listing.getTotalPrice());
        verify(repository).save(listing);
    }

    @Test
    @DisplayName("[Marketplace State: ACTIVE->RESERVED->SOLD] Buyer reserves & completes -> transitions to SOLD")
    void completePurchase_reservedListing_transitionsToSold() {
        Listing listing = new Listing("lst-1", "seller-1", "mat-alu", 20.0, 15.0);
        listing.reserve("buyer-9");
        when(repository.findById("lst-1")).thenReturn(Optional.of(listing));
        when(repository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        Listing sold = manager.completePurchase("lst-1");

        assertEquals(Listing.ListingStatus.SOLD, sold.getStatus());
        assertEquals("buyer-9", sold.getBuyerId());
        verify(repository).save(listing);
    }

    @Test
    @DisplayName("[Marketplace State Guard] Completing ACTIVE listing without reservation -> throws IllegalStateException")
    void completePurchase_activeListing_throwsIllegalStateException() {
        Listing listing = new Listing("lst-2", "seller-1", "mat-alu", 10.0, 15.0);
        when(repository.findById("lst-2")).thenReturn(Optional.of(listing));

        assertThrows(IllegalStateException.class, () -> manager.completePurchase("lst-2"));
        verify(repository, never()).save(any());
    }
}
