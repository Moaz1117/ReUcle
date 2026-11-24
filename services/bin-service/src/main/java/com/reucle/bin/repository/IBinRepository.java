package com.reucle.bin.repository;

import java.util.List;
import com.reucle.bin.domain.SmartBin;

public interface IBinRepository {
    void save(SmartBin bin);
    List<SmartBin> findNeedingCollection(float threshold);
    SmartBin findById(String binId);
}
