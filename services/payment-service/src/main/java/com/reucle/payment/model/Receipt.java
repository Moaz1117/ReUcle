package com.reucle.payment.model;

import java.time.Instant;

// PM2: payment-service Class Diagram -> Receipt
public record Receipt(String receiptId, String paymentId, double amount, Instant issuedAt) {}
