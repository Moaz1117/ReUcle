package com.reucle.bin.api;

import java.util.List;
import com.reucle.bin.domain.BinStatus;
import com.reucle.bin.domain.SensorReading;
import com.reucle.bin.domain.SmartBin;
import com.reucle.bin.dto.BinRegistrationData;

public interface IBin {
    void registerBin(BinRegistrationData data);
    BinStatus getBinStatus(String binId);
    List<SmartBin> getBinsNeedingCollection();
    List<SensorReading> getFillHistory(String binId);
}
