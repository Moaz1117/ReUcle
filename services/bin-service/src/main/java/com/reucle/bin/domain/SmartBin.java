package com.reucle.bin.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.reucle.bin.exceptions.InvalidBinStateTransitionException;
import com.reucle.bin.exceptions.InvalidReadingException;

public class SmartBin {
    private String binId;
    private String customerId;
    private String model;
    private float currentFillLevel;
    private BinStatus status;
    private final List<SensorReading> readings = new ArrayList<>();
    private WasteComposition composition;

    public SmartBin(String binId, String customerId, String model) {
        this.binId = binId;
        this.customerId = customerId;
        this.model = model;
        this.currentFillLevel = 0.0f;
        this.status = BinStatus.UNREGISTERED;
    }

    public SmartBin(String binId, String customerId, String model, float currentFillLevel, BinStatus status) {
        this.binId = binId;
        this.customerId = customerId;
        this.model = model;
        this.currentFillLevel = currentFillLevel;
        this.status = status;
    }

    public void activate() {
        if (this.status != BinStatus.UNREGISTERED) {
            throw new InvalidBinStateTransitionException(
                "activate() only allowed from UNREGISTERED; current: " + this.status
            );
        }
        this.status = BinStatus.ACTIVE;
    }

    public void updateReading(float fill, WasteComposition composition) {
        if (this.status == BinStatus.UNREGISTERED || this.status == BinStatus.RETIRED) {
            throw new InvalidBinStateTransitionException(
                "Cannot update reading in state: " + this.status
            );
        }
        if (fill < 0.0f || fill > 100.0f) {
            throw new InvalidReadingException("Fill level must be in [0, 100], got: " + fill);
        }
        this.currentFillLevel = fill;
        this.composition = composition;
        this.readings.add(new SensorReading(Instant.now(), fill));
    }

    public boolean checkThreshold(float threshold) {
        return this.currentFillLevel >= threshold;
    }

    public void markNeedsCollection() {
        if (this.status != BinStatus.ACTIVE && this.status != BinStatus.OFFLINE) {
            throw new InvalidBinStateTransitionException(
                "Cannot markNeedsCollection() from state: " + this.status
            );
        }
        this.status = BinStatus.NEEDS_COLLECTION;
    }

    public void resolveCollection() {
        if (this.status != BinStatus.NEEDS_COLLECTION) {
            throw new InvalidBinStateTransitionException(
                "resolveCollection() only allowed from NEEDS_COLLECTION; current: " + this.status
            );
        }
        this.currentFillLevel = 0.0f;
        this.status = BinStatus.ACTIVE;
    }

    public void markOffline() {
        if (this.status == BinStatus.UNREGISTERED || this.status == BinStatus.RETIRED) {
            throw new InvalidBinStateTransitionException(
                "Cannot markOffline() from state: " + this.status
            );
        }
        this.status = BinStatus.OFFLINE;
    }

    public void markOnline() {
        if (this.status != BinStatus.OFFLINE) {
            throw new InvalidBinStateTransitionException(
                "markOnline() only allowed from OFFLINE; current: " + this.status
            );
        }
        this.status = BinStatus.ACTIVE;
    }

    public void retire() {
        if (this.status == BinStatus.RETIRED) {
            throw new InvalidBinStateTransitionException("Bin is already RETIRED");
        }
        this.status = BinStatus.RETIRED;
    }

    public void addReading(SensorReading reading) {
        this.readings.add(reading);
    }

    public String getBinId() { return binId; }
    public String getCustomerId() { return customerId; }
    public String getModel() { return model; }
    public float getCurrentFillLevel() { return currentFillLevel; }
    public BinStatus getStatus() { return status; }
    public WasteComposition getComposition() { return composition; }
    public List<SensorReading> getReadings() { return Collections.unmodifiableList(readings); }
}
