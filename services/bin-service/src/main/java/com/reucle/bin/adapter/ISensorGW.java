package com.reucle.bin.adapter;

import com.reucle.bin.dto.SensorData;

public interface ISensorGW {
    SensorData receiveReading();
    boolean registerDevice();
}
