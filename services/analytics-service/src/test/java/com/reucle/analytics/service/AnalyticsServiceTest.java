package com.reucle.analytics.service;

import com.reucle.analytics.client.BinClient;
import com.reucle.analytics.engine.PredictionEngine;
import com.reucle.analytics.model.BinPrediction;
import com.reucle.analytics.model.Reading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// PM2 Test Map: analytics-service Class, Collaboration, Sequence & State Diagrams
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private BinClient binClient;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(binClient, new PredictionEngine());
    }

    @Test
    @DisplayName("[Analytics Sequence: Analyze Bin] Rising fill readings -> predicts URGENT_PICKUP")
    void analyzeBin_highFillTrend_returnsUrgentPrediction() {
        Instant t0 = Instant.parse("2026-11-28T08:00:00Z");
        Instant t1 = t0.plus(2, ChronoUnit.HOURS);
        List<Reading> readings = List.of(
                new Reading("r-1", "bin-1", 60.0, 18.0, t0),
                new Reading("r-2", "bin-1", 80.0, 24.0, t1) // +10%/hr -> 2h to full
        );
        when(binClient.fetchRecentReadings("bin-1")).thenReturn(readings);

        BinPrediction prediction = analyticsService.analyzeBin("bin-1", 2.0);

        assertEquals("bin-1", prediction.getBinId());
        assertEquals(100.0, prediction.getPredictedFillPercent(), 0.01);
        assertEquals(2.0, prediction.getEstimatedHoursToFull(), 0.01);
        assertEquals(BinPrediction.UrgencyState.URGENT_PICKUP, prediction.getUrgencyState());
        assertTrue(prediction.requiresImmediateDispatch());
    }

    @Test
    @DisplayName("[Analytics State: NORMAL] Low fill level -> classifies prediction as NORMAL")
    void analyzeBin_lowFill_returnsNormalPrediction() {
        Instant t0 = Instant.parse("2026-11-28T08:00:00Z");
        when(binClient.fetchRecentReadings("bin-2")).thenReturn(
                List.of(new Reading("r-3", "bin-2", 20.0, 5.0, t0))
        );

        BinPrediction prediction = analyticsService.analyzeBin("bin-2", 4.0);

        assertEquals(BinPrediction.UrgencyState.NORMAL, prediction.getUrgencyState());
        assertFalse(prediction.requiresImmediateDispatch());
    }

    @Test
    @DisplayName("[Analytics Collaboration: Urgent Filter] Active bins -> filters only URGENT_PICKUP bins")
    void getUrgentBins_mixedBinStates_returnsOnlyUrgentBins() {
        Instant t0 = Instant.parse("2026-11-28T08:00:00Z");
        when(binClient.getActiveBinIds()).thenReturn(List.of("bin-low", "bin-critical"));
        when(binClient.fetchRecentReadings("bin-low")).thenReturn(
                List.of(new Reading("r-1", "bin-low", 15.0, 4.0, t0))
        );
        when(binClient.fetchRecentReadings("bin-critical")).thenReturn(
                List.of(new Reading("r-2", "bin-critical", 92.0, 28.0, t0))
        );

        List<BinPrediction> urgent = analyticsService.getUrgentBins(1.0);

        assertEquals(1, urgent.size());
        assertEquals("bin-critical", urgent.get(0).getBinId());
        verify(binClient).getActiveBinIds();
    }

    @Test
    @DisplayName("[Analytics Guard] Empty reading history -> returns baseline 0.0% fill NORMAL prediction")
    void analyzeBin_emptyReadings_returnsBaselinePrediction() {
        when(binClient.fetchRecentReadings("bin-empty")).thenReturn(List.of());

        BinPrediction prediction = analyticsService.analyzeBin("bin-empty", 4.0);

        assertEquals(BinPrediction.UrgencyState.NORMAL, prediction.getUrgencyState());
        assertEquals(0.0, prediction.getPredictedFillPercent(), 0.01);
        assertEquals(40.0, prediction.getEstimatedHoursToFull(), 0.01);
    }
}
