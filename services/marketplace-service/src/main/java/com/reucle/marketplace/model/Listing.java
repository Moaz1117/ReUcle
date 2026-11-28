package com.reucle.marketplace.model;

import java.util.Objects;

// PM2: marketplace-service Class & State Diagram -> Listing
// States: ACTIVE -> RESERVED -> SOLD | CANCELLED
public class Listing {
    public enum ListingStatus {
        ACTIVE, RESERVED, SOLD, CANCELLED
    }

    private final String listingId;
    private final String sellerId;
    private final String materialId;
    private final double quantityKg;
    private final double pricePerKg;
    private String buyerId;
    private ListingStatus status;

    public Listing(String listingId, String sellerId, String materialId, double quantityKg, double pricePerKg) {
        this.listingId = Objects.requireNonNull(listingId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.materialId = Objects.requireNonNull(materialId);
        if (quantityKg <= 0 || pricePerKg < 0) {
            throw new IllegalArgumentException("Invalid quantity or price");
        }
        this.quantityKg = quantityKg;
        this.pricePerKg = pricePerKg;
        this.status = ListingStatus.ACTIVE;
    }

    public double getTotalPrice() {
        return quantityKg * pricePerKg;
    }

    // State Transition: [ACTIVE] --(reserve)--> [RESERVED]
    public void reserve(String buyerId) {
        if (this.status != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Cannot reserve listing in state: " + status);
        }
        this.buyerId = Objects.requireNonNull(buyerId);
        this.status = ListingStatus.RESERVED;
    }

    // State Transition: [RESERVED] --(confirmSale)--> [SOLD]
    public void markSold() {
        if (this.status != ListingStatus.RESERVED) {
            throw new IllegalStateException("Cannot mark listing SOLD from state: " + status);
        }
        this.status = ListingStatus.SOLD;
    }

    // State Transition: [ACTIVE | RESERVED] --(cancel)--> [CANCELLED]
    public void cancel() {
        if (this.status == ListingStatus.SOLD) {
            throw new IllegalStateException("Cannot cancel a SOLD listing");
        }
        this.status = ListingStatus.CANCELLED;
    }

    public String getListingId() { return listingId; }
    public String getSellerId() { return sellerId; }
    public String getMaterialId() { return materialId; }
    public double getQuantityKg() { return quantityKg; }
    public double getPricePerKg() { return pricePerKg; }
    public String getBuyerId() { return buyerId; }
    public ListingStatus getStatus() { return status; }
}
