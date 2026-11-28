package com.reucle.marketplace.repository;

import com.reucle.marketplace.model.Listing;
import java.util.List;
import java.util.Optional;

// PM2: marketplace-service Class/Collaboration Diagram -> IListingRepository
public interface IListingRepository {
    Optional<Listing> findById(String listingId);
    List<Listing> findByStatus(Listing.ListingStatus status);
    Listing save(Listing listing);
}
