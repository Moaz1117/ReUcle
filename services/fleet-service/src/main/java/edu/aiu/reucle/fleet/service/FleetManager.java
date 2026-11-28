package edu.aiu.reucle.fleet.service;

import edu.aiu.reucle.fleet.model.DriverAssignment;
import edu.aiu.reucle.fleet.model.Vehicle;
import edu.aiu.reucle.fleet.repository.IVehicleRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

// PM2: fleet-service Class/Collaboration/Sequence Diagram -> FleetManager
public class FleetManager {
    private final IVehicleRepository repository;

    public FleetManager(IVehicleRepository repository) {
        this.repository = repository;
    }

    // PM2: fleet-service Collaboration/Sequence (Assign & Dispatch) -> FleetManager.assignDriverAndDispatch
    public DriverAssignment assignDriverAndDispatch(String vehicleId, String driverId) {
        Vehicle vehicle = repository.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found: " + vehicleId));

        vehicle.dispatch();
        repository.save(vehicle);

        DriverAssignment assignment = new DriverAssignment(
                UUID.randomUUID().toString(),
                vehicleId,
                driverId
        );
        return repository.saveAssignment(assignment);
    }

    // PM2: fleet-service Sequence (Record Pickup Load) -> FleetManager.recordVehicleLoad
    public Vehicle recordVehicleLoad(String vehicleId, double weightKg) {
        Vehicle vehicle = repository.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found: " + vehicleId));

        vehicle.loadWaste(weightKg);
        return repository.save(vehicle);
    }

    // PM2: fleet-service Sequence (Complete Route) -> FleetManager.completeRouteAndReturn
    public Vehicle completeRouteAndReturn(String vehicleId) {
        Vehicle vehicle = repository.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found: " + vehicleId));

        vehicle.returnToDepot();
        return repository.save(vehicle);
    }

    public List<Vehicle> getAvailableVehicles() {
        return repository.findByStatus(Vehicle.VehicleStatus.AVAILABLE);
    }
}
