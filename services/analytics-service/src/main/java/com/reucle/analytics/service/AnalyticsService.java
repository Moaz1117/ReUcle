package com.reucle.analytics.service;

import com.reucle.analytics.client.BinClient;
import com.reucle.analytics.engine.PredictionEngine;
import com.reucle.analytics.model.BinPrediction;
import com.reucle.analytics.model.Reading;
import java.util.List;
import java.util.Objects;

// PM2: analytics-service Class, Collaboration & Sequence Diagram -> AnalyticsService
public class AnalyticsService {
    private final BinClient binClient;
    private final PredictionEngine predictionEngine;

    public AnalyticsService(BinClient binClient, PredictionEngine predictionEngine) {
        this.binClient = Objects.requireNonNull(binClient);
        this.predictionEngine = Objects.requireNonNull(predictionEngine);
    }

    // PM2: analytics-service Sequence (Analyze Single Bin) -> AnalyticsService.analyzeBin
    public BinPrediction analyzeBin(String binId, double horizonHours) {
        List<Reading> readings = binClient.fetchRecentReadings(binId);
        return predictionEngine.predictFill(binId, readings, horizonHours);
    }

    // PM2: analytics-service Collaboration (Batch Priority Scan) -> AnalyticsService.getUrgentBins
    public List<BinPrediction> getUrgentBins(double horizonHours) {
        return binClient.getActiveBinIds().stream()
                .map(binId -> analyzeBin(binId, horizonHours))
                .filter(BinPrediction::requiresImmediateDispatch)
                .toList();
    }
}
