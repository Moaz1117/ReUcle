package edu.aiu.reucle.collection.service;

import edu.aiu.reucle.collection.client.IFleetClient;
import edu.aiu.reucle.collection.model.CollectionTask;
import edu.aiu.reucle.collection.model.RoutePlan;
import edu.aiu.reucle.collection.repository.ICollectionRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

// PM2: collection-service Class/Collaboration/Sequence Diagram -> CollectionManager
public class CollectionManager {
    private final ICollectionRepository repository;
    private final IFleetClient fleetClient;

    public CollectionManager(ICollectionRepository repository, IFleetClient fleetClient) {
        this.repository = repository;
        this.fleetClient = fleetClient;
    }

    // PM2: collection-service Sequence (Schedule Pickup) -> CollectionManager.schedulePickup
    public CollectionTask schedulePickup(String binId) {
        CollectionTask task = new CollectionTask(UUID.randomUUID().toString(), binId);
        return repository.save(task);
    }

    // PM2: collection-service Collaboration/Sequence (Dispatch Vehicle) -> CollectionManager.dispatchTask
    public CollectionTask dispatchTask(String taskId, String vehicleId) {
        CollectionTask task = repository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("CollectionTask not found: " + taskId));

        boolean reserved = fleetClient.reserveVehicle(vehicleId, taskId);
        if (!reserved) {
            throw new IllegalStateException("Vehicle unavailable for dispatch: " + vehicleId);
        }

        task.assignVehicle(vehicleId);
        return repository.save(task);
    }

    // PM2: collection-service Sequence (Execute & Complete Pickup) -> CollectionManager.completePickup
    public CollectionTask completePickup(String taskId, double collectedWeightKg) {
        CollectionTask task = repository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("CollectionTask not found: " + taskId));

        task.start();
        task.complete(collectedWeightKg);
        fleetClient.releaseVehicle(task.getAssignedVehicleId());
        return repository.save(task);
    }

    public RoutePlan buildRoutePlan(String vehicleId, List<String> binIds) {
        return new RoutePlan(UUID.randomUUID().toString(), vehicleId, binIds);
    }
}
