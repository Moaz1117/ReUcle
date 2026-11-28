package com.reucle.reward.service;

// PM2: reward-service Class/Collaboration Diagram (US38) -> RewardCalculator
public class RewardCalculator {
    public double calculate(double weightKg, double ratePerKg) {
        if (weightKg <= 0 || ratePerKg < 0) {
            throw new IllegalArgumentException("Invalid weight or rate");
        }
        return weightKg * ratePerKg;
    }
}
