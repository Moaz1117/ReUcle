package com.reucle.user.domain;

import java.time.Instant;

public class AuditLog {
    private String logId;
    private String userId;
    private String action;
    private Instant timestamp;

    public AuditLog(String logId, String userId, String action, Instant timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.timestamp = timestamp;
    }

    public String getLogId() { return logId; }
    public String getUserId() { return userId; }
    public String getAction() { return action; }
    public Instant getTimestamp() { return timestamp; }
}
