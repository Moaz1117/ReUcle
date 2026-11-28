package com.reucle.analytics.model;

import java.time.Instant;
import java.util.Objects;

// PM2: analytics-service Class, Object & State Diagram -> BinPrediction
// States: NORMAL -> NEAR_FULL -> URGENT_PICKUP
public class BinPrediction {
    public enum UrgencyState {
        NORMAL, NEAR_FULL, URGENT_PICKUP
    }

    private final String predictionId;
    private final String binId;
    private final double predictedFillPercent;
    private final double estimatedHoursToFull;
    private final UrgencyState urgencyState;
    private final Instant generatedAt;

    public BinPrediction(String predictionId, String binId, double predictedFillPercent, double estimatedHoursToFull) {
        this.predictionId = Objects.requireNonNull(predictionId);
        this.binId = Objects.requireNonNull(binId);
        if (predictedFillPercent < 0.0 || predictedFillPercent > 100.0) {
            throw new IllegalArgumentException("predictedFillPercent must be between 0 and 100");
        }
        if (estimatedHoursToFull < 0.0) {
            throw new IllegalArgumentException("estimatedHoursToFull cannot be negative");
        }
        this.predictedFillPercent = predictedFillPercent;
        this.estimatedHoursToFull = estimatedHoursToFull;
        this.urgencyState = classifyUrgency(predictedFillPercent, estimatedHoursToFull);
        this.generatedAt = Instant.now();
    }

    // PM2: analytics-service State Diagram -> classifyUrgency
    private static UrgencyState classifyUrgency(double predictedFill, double hoursToFull) {
        if (predictedFill >= 90.0 || hoursToFull <= 4.0) {
            return UrgencyState.URGENT_PICKUP;
        }
        if (predictedFill >= 75.0 || hoursToFull <= 12.0) {
            return UrgencyState.NEAR_FULL;
        }
        return UrgencyState.NORMAL;
    }

    public boolean requiresImmediateDispatch() {
        return urgencyState == UrgencyState.URGENT_PICKUP;
    }

    public String getPredictionId() { return predictionId; }
    public String getBinId() { return binId; }
    public double getPredictedFillPercent() { return predictedFillPercent; }
    public double getEstimatedHoursToFull() { return estimatedHoursToFull; }
    public UrgencyState getUrgencyState() { return urgencyState; }
    public Instant getGeneratedAt() { return generatedAt; }
}
