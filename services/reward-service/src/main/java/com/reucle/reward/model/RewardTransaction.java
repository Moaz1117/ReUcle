package com.reucle.reward.model;

import java.time.Instant;
import java.util.Objects;

// PM2: reward-service Class/Object Diagram (US38) -> RewardTransaction
public class RewardTransaction {
    public enum TransactionType { CREDIT, REDEEM }

    private final String transactionId;
    private final String customerId;
    private final double points;
    private final TransactionType type;
    private final Instant timestamp;

    public RewardTransaction(String transactionId, String customerId, double points, TransactionType type) {
        this.transactionId = Objects.requireNonNull(transactionId);
        this.customerId = Objects.requireNonNull(customerId);
        this.points = points;
        this.type = Objects.requireNonNull(type);
        this.timestamp = Instant.now();
    }

    public String getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public double getPoints() { return points; }
    public TransactionType getType() { return type; }
    public Instant getTimestamp() { return timestamp; }
}
