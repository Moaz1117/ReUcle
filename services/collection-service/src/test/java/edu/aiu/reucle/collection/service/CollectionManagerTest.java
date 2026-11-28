package edu.aiu.reucle.collection.service;

import edu.aiu.reucle.collection.client.IFleetClient;
import edu.aiu.reucle.collection.model.CollectionTask;
import edu.aiu.reucle.collection.model.RoutePlan;
import edu.aiu.reucle.collection.repository.ICollectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// PM2 Test Map: collection-service Class, Collaboration, Sequence & State Diagrams
@ExtendWith(MockitoExtension.class)
class CollectionManagerTest {

    @Mock
    private ICollectionRepository repository;

    @Mock
    private IFleetClient fleetClient;

    private CollectionManager manager;

    @BeforeEach
    void setUp() {
        manager = new CollectionManager(repository, fleetClient);
    }

    @Test
    @DisplayName("[Collection Sequence: Schedule] New bin pickup -> creates SCHEDULED CollectionTask")
    void schedulePickup_validBin_createsScheduledTask() {
        when(repository.save(any(CollectionTask.class))).thenAnswer(inv -> inv.getArgument(0));

        CollectionTask task = manager.schedulePickup("bin-101");

        assertEquals("bin-101", task.getBinId());
        assertEquals(CollectionTask.TaskStatus.SCHEDULED, task.getStatus());
        verify(repository).save(task);
    }

    @Test
    @DisplayName("[Collection Collaboration: Dispatch] Fleet reserves vehicle -> transitions SCHEDULED -> ASSIGNED")
    void dispatchTask_vehicleAvailable_transitionsToAssigned() {
        CollectionTask task = new CollectionTask("task-1", "bin-101");
        when(repository.findById("task-1")).thenReturn(Optional.of(task));
        when(fleetClient.reserveVehicle("veh-1", "task-1")).thenReturn(true);
        when(repository.save(any(CollectionTask.class))).thenAnswer(inv -> inv.getArgument(0));

        CollectionTask result = manager.dispatchTask("task-1", "veh-1");

        assertEquals(CollectionTask.TaskStatus.ASSIGNED, result.getStatus());
        assertEquals("veh-1", result.getAssignedVehicleId());
        verify(repository).save(task);
    }

    @Test
    @DisplayName("[Collection State: ASSIGNED->IN_PROGRESS->COMPLETED] Completing pickup -> records weight & releases vehicle")
    void completePickup_assignedTask_transitionsToCompletedAndReleasesVehicle() {
        CollectionTask task = new CollectionTask("task-1", "bin-101");
        task.assignVehicle("veh-1");
        when(repository.findById("task-1")).thenReturn(Optional.of(task));
        when(repository.save(any(CollectionTask.class))).thenAnswer(inv -> inv.getArgument(0));

        CollectionTask completed = manager.completePickup("task-1", 45.5);

        assertEquals(CollectionTask.TaskStatus.COMPLETED, completed.getStatus());
        assertEquals(45.5, completed.getCollectedWeightKg());
        verify(fleetClient).releaseVehicle("veh-1");
        verify(repository).save(task);
    }

    @Test
    @DisplayName("[Collection State Guard] Completing un-assigned task -> throws IllegalStateException")
    void complete_scheduledTaskWithoutAssignment_throwsIllegalStateException() {
        CollectionTask task = new CollectionTask("task-2", "bin-102");
        assertThrows(IllegalStateException.class, () -> task.complete(10.0));
        verifyNoInteractions(repository, fleetClient);
    }

    @Test
    @DisplayName("[Collection Object Diagram: RoutePlan] Valid bin list -> builds immutable RoutePlan")
    void buildRoutePlan_validBins_returnsRoutePlan() {
        RoutePlan plan = manager.buildRoutePlan("veh-1", List.of("bin-1", "bin-2"));
        assertEquals("veh-1", plan.getVehicleId());
        assertEquals(2, plan.getBinIds().size());
        verifyNoInteractions(repository, fleetClient);
    }
}
