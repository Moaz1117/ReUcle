package edu.aiu.reucle.fleet.model;

import java.time.Instant;
import java.util.Objects;

// PM2: fleet-service Class/Object Diagram -> DriverAssignment
public class DriverAssignment {
    private final String assignmentId;
    private final String vehicleId;
    private final String driverId;
    private final Instant assignedAt;

    public DriverAssignment(String assignmentId, String vehicleId, String driverId) {
        this.assignmentId = Objects.requireNonNull(assignmentId);
        this.vehicleId = Objects.requireNonNull(vehicleId);
        this.driverId = Objects.requireNonNull(driverId);
        this.assignedAt = Instant.now();
    }

    public String getAssignmentId() { return assignmentId; }
    public String getVehicleId() { return vehicleId; }
    public String getDriverId() { return driverId; }
    public Instant getAssignedAt() { return assignedAt; }
}
