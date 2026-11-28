package com.reucle.marketplace.service;

import com.reucle.marketplace.model.Listing;
import com.reucle.marketplace.repository.IListingRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

// PM2: marketplace-service Class/Collaboration/Sequence Diagram -> MarketplaceManager
public class MarketplaceManager {
    private final IListingRepository repository;

    public MarketplaceManager(IListingRepository repository) {
        this.repository = repository;
    }

    // PM2: marketplace-service Sequence (Create Listing) -> MarketplaceManager.createListing
    public Listing createListing(String sellerId, String materialId, double quantityKg, double pricePerKg) {
        Listing listing = new Listing(UUID.randomUUID().toString(), sellerId, materialId, quantityKg, pricePerKg);
        return repository.save(listing);
    }

    // PM2: marketplace-service Sequence (Reserve Listing) -> MarketplaceManager.reserveListing
    public Listing reserveListing(String listingId, String buyerId) {
        Listing listing = repository.findById(listingId)
                .orElseThrow(() -> new NoSuchElementException("Listing not found: " + listingId));
        listing.reserve(buyerId);
        return repository.save(listing);
    }

    // PM2: marketplace-service Sequence (Complete Order) -> MarketplaceManager.completePurchase
    public Listing completePurchase(String listingId) {
        Listing listing = repository.findById(listingId)
                .orElseThrow(() -> new NoSuchElementException("Listing not found: " + listingId));
        listing.markSold();
        return repository.save(listing);
    }

    public List<Listing> getActiveListings() {
        return repository.findByStatus(Listing.ListingStatus.ACTIVE);
    }
}
