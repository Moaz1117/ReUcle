package edu.aiu.reucle.collection.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

// PM2: collection-service Class/Object Diagram -> RoutePlan
public class RoutePlan {
    private final String routeId;
    private final String vehicleId;
    private final List<String> binIds;

    public RoutePlan(String routeId, String vehicleId, List<String> binIds) {
        this.routeId = Objects.requireNonNull(routeId);
        this.vehicleId = Objects.requireNonNull(vehicleId);
        if (binIds == null || binIds.isEmpty()) {
            throw new IllegalArgumentException("RoutePlan requires at least one binId");
        }
        this.binIds = List.copyOf(binIds);
    }

    public String getRouteId() { return routeId; }
    public String getVehicleId() { return vehicleId; }
    public List<String> getBinIds() { return Collections.unmodifiableList(binIds); }
}
