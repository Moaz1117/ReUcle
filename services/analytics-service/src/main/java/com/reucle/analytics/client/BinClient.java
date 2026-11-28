package com.reucle.analytics.client;

import com.reucle.analytics.model.Reading;
import java.util.List;

// PM2: analytics-service Class, Collaboration & Sequence Diagram -> BinClient
public interface BinClient {
    List<Reading> fetchRecentReadings(String binId);
    List<String> getActiveBinIds();
}
