package edu.aiu.reucle.collection.model;

import java.time.Instant;
import java.util.Objects;

// PM2: collection-service Class & State Diagram -> CollectionTask
// States: SCHEDULED -> ASSIGNED -> IN_PROGRESS -> COMPLETED | CANCELLED
public class CollectionTask {
    public enum TaskStatus {
        SCHEDULED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    private final String taskId;
    private final String binId;
    private String assignedVehicleId;
    private double collectedWeightKg;
    private TaskStatus status;
    private final Instant scheduledAt;

    public CollectionTask(String taskId, String binId) {
        this.taskId = Objects.requireNonNull(taskId);
        this.binId = Objects.requireNonNull(binId);
        this.status = TaskStatus.SCHEDULED;
        this.scheduledAt = Instant.now();
    }

    // State Transition: [SCHEDULED] --(assignVehicle)--> [ASSIGNED]
    public void assignVehicle(String vehicleId) {
        if (this.status != TaskStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot assign vehicle when task is in state: " + status);
        }
        this.assignedVehicleId = Objects.requireNonNull(vehicleId);
        this.status = TaskStatus.ASSIGNED;
    }

    // State Transition: [ASSIGNED] --(startCollection)--> [IN_PROGRESS]
    public void start() {
        if (this.status != TaskStatus.ASSIGNED) {
            throw new IllegalStateException("Cannot start task in state: " + status);
        }
        this.status = TaskStatus.IN_PROGRESS;
    }

    // State Transition: [IN_PROGRESS] --(completePickup)--> [COMPLETED]
    public void complete(double weightKg) {
        if (this.status != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete task in state: " + status);
        }
        if (weightKg <= 0) {
            throw new IllegalArgumentException("Collected weight must be positive");
        }
        this.collectedWeightKg = weightKg;
        this.status = TaskStatus.COMPLETED;
    }

    // State Transition: [SCHEDULED | ASSIGNED] --(cancel)--> [CANCELLED]
    public void cancel() {
        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot cancel task in state: " + status);
        }
        this.status = TaskStatus.CANCELLED;
    }

    public String getTaskId() { return taskId; }
    public String getBinId() { return binId; }
    public String getAssignedVehicleId() { return assignedVehicleId; }
    public double getCollectedWeightKg() { return collectedWeightKg; }
    public TaskStatus getStatus() { return status; }
    public Instant getScheduledAt() { return scheduledAt; }
}
