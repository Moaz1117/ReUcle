package com.reucle.payment.model;

import java.time.Instant;
import java.util.Objects;

// PM2: payment-service Class Diagram & PaymentRecord State Diagram
// States: PENDING -> PAID | FAILED; PAID -> REFUNDED
public class PaymentRecord {
    public enum PaymentStatus {
        PENDING, PAID, FAILED, REFUNDED
    }

    private final String paymentId;
    private final String userId;
    private final double amount;
    private PaymentStatus status;
    private final Instant createdAt;

    public PaymentRecord(String paymentId, String userId, double amount) {
        this.paymentId = Objects.requireNonNull(paymentId);
        this.userId = Objects.requireNonNull(userId);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
    }

    // State Transition: [PENDING] --(gatewaySuccess)--> [PAID]
    public void markPaid() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Invalid transition from " + status + " to PAID");
        }
        this.status = PaymentStatus.PAID;
    }

    // State Transition: [PENDING] --(gatewayFailure)--> [FAILED]
    public void markFailed() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Invalid transition from " + status + " to FAILED");
        }
        this.status = PaymentStatus.FAILED;
    }

    // State Transition: [PAID] --(refundRequested)--> [REFUNDED]
    public void refund() {
        if (this.status != PaymentStatus.PAID) {
            throw new IllegalStateException("Invalid transition from " + status + " to REFUNDED");
        }
        this.status = PaymentStatus.REFUNDED;
    }

    public String getPaymentId() { return paymentId; }
    public String getUserId() { return userId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
