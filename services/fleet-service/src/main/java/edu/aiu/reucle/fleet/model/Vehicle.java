package edu.aiu.reucle.fleet.model;

import java.util.Objects;

// PM2: fleet-service Class & State Diagram -> Vehicle
// States: AVAILABLE <-> ON_ROUTE; AVAILABLE <-> MAINTENANCE
public class Vehicle {
    public enum VehicleStatus {
        AVAILABLE, ON_ROUTE, MAINTENANCE
    }

    private final String vehicleId;
    private final String plateNumber;
    private final double capacityKg;
    private double currentLoadKg;
    private VehicleStatus status;

    public Vehicle(String vehicleId, String plateNumber, double capacityKg) {
        this.vehicleId = Objects.requireNonNull(vehicleId);
        this.plateNumber = Objects.requireNonNull(plateNumber);
        if (capacityKg <= 0) {
            throw new IllegalArgumentException("Vehicle capacity must be positive");
        }
        this.capacityKg = capacityKg;
        this.currentLoadKg = 0.0;
        this.status = VehicleStatus.AVAILABLE;
    }

    // State Transition: [AVAILABLE] --(dispatch)--> [ON_ROUTE]
    public void dispatch() {
        if (this.status != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Vehicle cannot be dispatched from state: " + status);
        }
        this.status = VehicleStatus.ON_ROUTE;
    }

    // PM2: fleet-service Sequence -> Vehicle.loadWaste(weightKg)
    public void loadWaste(double weightKg) {
        if (weightKg <= 0) {
            throw new IllegalArgumentException("Load weight must be positive");
        }
        if (this.currentLoadKg + weightKg > this.capacityKg) {
            throw new IllegalStateException("Vehicle capacity exceeded for: " + vehicleId);
        }
        this.currentLoadKg += weightKg;
    }

    // State Transition: [ON_ROUTE] --(returnToDepot)--> [AVAILABLE]
    public void returnToDepot() {
        if (this.status != VehicleStatus.ON_ROUTE) {
            throw new IllegalStateException("Only ON_ROUTE vehicles can return to depot");
        }
        this.currentLoadKg = 0.0;
        this.status = VehicleStatus.AVAILABLE;
    }

    // State Transition: [AVAILABLE] --(scheduleMaintenance)--> [MAINTENANCE]
    public void sendToMaintenance() {
        if (this.status != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot send vehicle to maintenance from state: " + status);
        }
        this.status = VehicleStatus.MAINTENANCE;
    }

    // State Transition: [MAINTENANCE] --(completeMaintenance)--> [AVAILABLE]
    public void completeMaintenance() {
        if (this.status != VehicleStatus.MAINTENANCE) {
            throw new IllegalStateException("Vehicle is not in MAINTENANCE");
        }
        this.status = VehicleStatus.AVAILABLE;
    }

    public String getVehicleId() { return vehicleId; }
    public String getPlateNumber() { return plateNumber; }
    public double getCapacityKg() { return capacityKg; }
    public double getCurrentLoadKg() { return currentLoadKg; }
    public VehicleStatus getStatus() { return status; }
}
