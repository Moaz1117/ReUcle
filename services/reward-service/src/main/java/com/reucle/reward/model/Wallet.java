package com.reucle.reward.model;

import java.util.Objects;

// PM2: reward-service Class/Collaboration Diagram (US38) -> Wallet
public class Wallet {
    private final String walletId;
    private final String customerId;
    private double balance;

    public Wallet(String walletId, String customerId, double initialBalance) {
        this.walletId = Objects.requireNonNull(walletId);
        this.customerId = Objects.requireNonNull(customerId);
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.balance = initialBalance;
    }

    // PM2: reward-service Collaboration/Sequence (US38) -> Wallet.credit(points)
    public void credit(double points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        this.balance += points;
    }

    public void debit(double points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Debit points must be positive");
        }
        if (points > this.balance) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.balance -= points;
    }

    public String getWalletId() { return walletId; }
    public String getCustomerId() { return customerId; }
    public double getBalance() { return balance; }
}
