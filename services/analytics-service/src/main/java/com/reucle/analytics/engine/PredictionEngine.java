package com.reucle.analytics.engine;

import com.reucle.analytics.model.BinPrediction;
import com.reucle.analytics.model.Reading;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

// PM2: analytics-service Class, Collaboration & Sequence Diagram -> PredictionEngine
public class PredictionEngine {
    private static final double DEFAULT_FILL_RATE_PER_HOUR = 2.5;

    // PM2: analytics-service Sequence (Generate Bin Prediction) -> PredictionEngine.predictFill
    public BinPrediction predictFill(String binId, List<Reading> readings, double horizonHours) {
        if (horizonHours <= 0) {
            throw new IllegalArgumentException("horizonHours must be positive");
        }
        if (readings == null || readings.isEmpty()) {
            return new BinPrediction(UUID.randomUUID().toString(), binId, 0.0, 100.0 / DEFAULT_FILL_RATE_PER_HOUR);
        }

        List<Reading> sorted = readings.stream()
                .sorted(Comparator.comparing(Reading::getRecordedAt))
                .toList();

        Reading latest = sorted.get(sorted.size() - 1);
        double currentFill = latest.getFillLevelPercent();
        double ratePerHour = computeFillRatePerHour(sorted);

        double projectedFill = Math.min(100.0, currentFill + (ratePerHour * horizonHours));
        double remainingCapacity = Math.max(0.0, 100.0 - currentFill);
        double hoursToFull = ratePerHour > 0.0 ? remainingCapacity / ratePerHour : Double.POSITIVE_INFINITY;

        return new BinPrediction(UUID.randomUUID().toString(), binId, projectedFill, hoursToFull);
    }

    private double computeFillRatePerHour(List<Reading> sorted) {
        if (sorted.size() < 2) {
            return DEFAULT_FILL_RATE_PER_HOUR;
        }
        Reading first = sorted.get(0);
        Reading last = sorted.get(sorted.size() - 1);
        long minutes = Duration.between(first.getRecordedAt(), last.getRecordedAt()).toMinutes();
        if (minutes <= 0) {
            return DEFAULT_FILL_RATE_PER_HOUR;
        }
        double deltaFill = Math.max(0.0, last.getFillLevelPercent() - first.getFillLevelPercent());
        double rate = deltaFill / (minutes / 60.0);
        return rate > 0.0 ? rate : DEFAULT_FILL_RATE_PER_HOUR;
    }
}
