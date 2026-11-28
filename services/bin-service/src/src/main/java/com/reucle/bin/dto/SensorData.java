package com.reucle.bin.dto;

import java.time.Instant;
import com.reucle.bin.domain.WasteComposition;

public record SensorData(
    String binId,
    float fillLevel,
    WasteComposition composition,
    Instant timestamp
) {}
