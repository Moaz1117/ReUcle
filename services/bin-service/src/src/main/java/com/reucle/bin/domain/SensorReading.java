package com.reucle.bin.domain;

import java.time.Instant;

public class SensorReading {
    private Instant timestamp;
    private float fillLevel;

    public SensorReading(Instant timestamp, float fillLevel) {
        this.timestamp = timestamp;
        this.fillLevel = fillLevel;
    }

    public Instant getTimestamp() { return timestamp; }
    public float getFillLevel() { return fillLevel; }
}
