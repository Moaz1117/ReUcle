package com.reucle.bin.service;

import java.util.List;
import com.reucle.bin.adapter.ISensorGW;
import com.reucle.bin.api.IBin;
import com.reucle.bin.domain.BinStatus;
import com.reucle.bin.domain.SensorReading;
import com.reucle.bin.domain.SmartBin;
import com.reucle.bin.dto.BinRegistrationData;
import com.reucle.bin.dto.SensorData;
import com.reucle.bin.exceptions.BinNotFoundException;
import com.reucle.bin.exceptions.DeviceRegistrationException;
import com.reucle.bin.repository.IBinRepository;

public class BinManager implements IBin {
    public static final float COLLECTION_THRESHOLD = 80.0f;

    private final IBinRepository binRepo;
    private final ISensorGW sensorGateway;

    public BinManager(IBinRepository binRepo, ISensorGW sensorGateway) {
        this.binRepo = binRepo;
        this.sensorGateway = sensorGateway;
    }

    public void processIncomingReading() {
        SensorData data1 = sensorGateway.receiveReading();
        SmartBin bin42 = binRepo.findById(data1.binId());
        if (bin42 == null) {
            throw new BinNotFoundException("Bin not found: " + data1.binId());
        }
        bin42.updateReading(data1.fillLevel(), data1.composition());
        if (bin42.checkThreshold(COLLECTION_THRESHOLD)) {
            bin42.markNeedsCollection();
        }
        binRepo.save(bin42);
    }

    @Override
    public void registerBin(BinRegistrationData data) {
        if (!sensorGateway.registerDevice()) {
            throw new DeviceRegistrationException("Failed to register device: " + data.binId());
        }
        SmartBin bin = new SmartBin(data.binId(), data.customerId(), data.model());
        bin.activate();
        binRepo.save(bin);
    }

    @Override
    public BinStatus getBinStatus(String binId) {
        SmartBin bin = binRepo.findById(binId);
        if (bin == null) {
            throw new BinNotFoundException("Bin not found: " + binId);
        }
        return bin.getStatus();
    }

    @Override
    public List<SmartBin> getBinsNeedingCollection() {
        return binRepo.findNeedingCollection(COLLECTION_THRESHOLD);
    }

    @Override
    public List<SensorReading> getFillHistory(String binId) {
        SmartBin bin = binRepo.findById(binId);
        if (bin == null) {
            throw new BinNotFoundException("Bin not found: " + binId);
        }
        return bin.getReadings();
    }
}
