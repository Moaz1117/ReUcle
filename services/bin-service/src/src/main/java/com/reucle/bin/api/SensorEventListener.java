package com.reucle.bin.api;

import com.reucle.bin.service.BinManager;

public class SensorEventListener {
    private final BinManager binManager;

    public SensorEventListener(BinManager binManager) {
        this.binManager = binManager;
    }

    public void onSensorEvent() {
        binManager.processIncomingReading();
    }
}
