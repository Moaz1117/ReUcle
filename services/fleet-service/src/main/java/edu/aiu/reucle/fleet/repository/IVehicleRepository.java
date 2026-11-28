package edu.aiu.reucle.fleet.repository;

import edu.aiu.reucle.fleet.model.DriverAssignment;
import edu.aiu.reucle.fleet.model.Vehicle;
import java.util.List;
import java.util.Optional;

// PM2: fleet-service Class/Collaboration Diagram -> IVehicleRepository
public interface IVehicleRepository {
    Optional<Vehicle> findById(String vehicleId);
    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);
    Vehicle save(Vehicle vehicle);
    DriverAssignment saveAssignment(DriverAssignment assignment);
}
