package edu.aiu.reucle.collection.client;

// PM2: collection-service Collaboration/Sequence Diagram -> IFleetClient
public interface IFleetClient {
    boolean reserveVehicle(String vehicleId, String taskId);
    void releaseVehicle(String vehicleId);
}
