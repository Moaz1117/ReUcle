package com.reucle.analytics.model;

import java.time.Instant;
import java.util.Objects;

// PM2: analytics-service Class & Object Diagram -> Reading
public class Reading {
    private final String readingId;
    private final String binId;
    private final double fillLevelPercent;
    private final double weightKg;
    private final Instant recordedAt;

    public Reading(String readingId, String binId, double fillLevelPercent, double weightKg, Instant recordedAt) {
        this.readingId = Objects.requireNonNull(readingId);
        this.binId = Objects.requireNonNull(binId);
        if (fillLevelPercent < 0.0 || fillLevelPercent > 100.0) {
            throw new IllegalArgumentException("fillLevelPercent must be between 0 and 100");
        }
        if (weightKg < 0.0) {
            throw new IllegalArgumentException("weightKg cannot be negative");
        }
        this.fillLevelPercent = fillLevelPercent;
        this.weightKg = weightKg;
        this.recordedAt = Objects.requireNonNull(recordedAt);
    }

    public String getReadingId() { return readingId; }
    public String getBinId() { return binId; }
    public double getFillLevelPercent() { return fillLevelPercent; }
    public double getWeightKg() { return weightKg; }
    public Instant getRecordedAt() { return recordedAt; }
}
