package edu.aiu.reucle.fleet.service;

import edu.aiu.reucle.fleet.model.DriverAssignment;
import edu.aiu.reucle.fleet.model.Vehicle;
import edu.aiu.reucle.fleet.repository.IVehicleRepository;
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

// PM2 Test Map: fleet-service Class, Collaboration, Sequence & State Diagrams
@ExtendWith(MockitoExtension.class)
class FleetManagerTest {

    @Mock
    private IVehicleRepository repository;

    private FleetManager manager;

    @BeforeEach
    void setUp() {
        manager = new FleetManager(repository);
    }

    @Test
    @DisplayName("[Fleet State: AVAILABLE->ON_ROUTE] Dispatching vehicle -> creates DriverAssignment & sets ON_ROUTE")
    void assignDriverAndDispatch_availableVehicle_transitionsToOnRoute() {
        Vehicle truck = new Vehicle("veh-1", "ALX-1234", 1000.0);
        when(repository.findById("veh-1")).thenReturn(Optional.of(truck));
        when(repository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.saveAssignment(any(DriverAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        DriverAssignment assignment = manager.assignDriverAndDispatch("veh-1", "drv-9");

        assertEquals(Vehicle.VehicleStatus.ON_ROUTE, truck.getStatus());
        assertEquals("drv-9", assignment.getDriverId());
        verify(repository).save(truck);
        verify(repository).saveAssignment(any(DriverAssignment.class));
    }

    @Test
    @DisplayName("[Fleet Sequence: Load & Return] Loading within capacity and returning -> resets load & sets AVAILABLE")
    void completeRouteAndReturn_onRouteVehicle_resetsLoadAndSetsAvailable() {
        Vehicle truck = new Vehicle("veh-1", "ALX-1234", 1000.0);
        truck.dispatch();
        when(repository.findById("veh-1")).thenReturn(Optional.of(truck));
        when(repository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        manager.recordVehicleLoad("veh-1", 350.0);
        assertEquals(350.0, truck.getCurrentLoadKg());

        Vehicle returned = manager.completeRouteAndReturn("veh-1");
        assertEquals(0.0, returned.getCurrentLoadKg());
        assertEquals(Vehicle.VehicleStatus.AVAILABLE, returned.getStatus());
        verify(repository, times(2)).save(truck);
    }

    @Test
    @DisplayName("[Fleet Guard] Loading above capacityKg -> throws IllegalStateException")
    void recordVehicleLoad_exceedsCapacity_throwsIllegalStateException() {
        Vehicle truck = new Vehicle("veh-1", "ALX-1234", 500.0);
        when(repository.findById("veh-1")).thenReturn(Optional.of(truck));

        assertThrows(IllegalStateException.class, () -> manager.recordVehicleLoad("veh-1", 600.0));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("[Fleet State: AVAILABLE<->MAINTENANCE] Maintenance cycle -> toggles AVAILABLE and MAINTENANCE")
    void maintenanceCycle_availableVehicle_togglesStatus() {
        Vehicle truck = new Vehicle("veh-2", "ALX-5678", 800.0);
        truck.sendToMaintenance();
        assertEquals(Vehicle.VehicleStatus.MAINTENANCE, truck.getStatus());

        truck.completeMaintenance();
        assertEquals(Vehicle.VehicleStatus.AVAILABLE, truck.getStatus());
        verifyNoInteractions(repository);
    }
}
