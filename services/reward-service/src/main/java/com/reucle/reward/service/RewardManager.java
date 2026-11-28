package com.reucle.reward.service;

import com.reucle.reward.model.RewardTransaction;
import com.reucle.reward.model.Wallet;
import com.reucle.reward.repository.IWalletRepository;
import java.util.NoSuchElementException;
import java.util.UUID;

// PM2: reward-service Collaboration/Sequence Diagram (US38) -> RewardManager
public class RewardManager {
    private final IWalletRepository walletRepository;
    private final RewardCalculator rewardCalculator;

    public RewardManager(IWalletRepository walletRepository, RewardCalculator rewardCalculator) {
        this.walletRepository = walletRepository;
        this.rewardCalculator = rewardCalculator;
    }

    // PM2: reward-service Collaboration/Sequence (US38) -> RewardManager.creditCustomerReward
    public RewardTransaction creditCustomerReward(String customerId, double weightKg, double ratePerKg) {
        double points = rewardCalculator.calculate(weightKg, ratePerKg);

        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseGet(() -> new Wallet(UUID.randomUUID().toString(), customerId, 0.0));

        wallet.credit(points);
        walletRepository.saveWallet(wallet);

        RewardTransaction tx = new RewardTransaction(
                UUID.randomUUID().toString(),
                customerId,
                points,
                RewardTransaction.TransactionType.CREDIT
        );
        return walletRepository.saveTransaction(tx);
    }

    public RewardTransaction redeemPoints(String customerId, double points) {
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new NoSuchElementException("Wallet not found for customer: " + customerId));
        wallet.debit(points);
        walletRepository.saveWallet(wallet);

        RewardTransaction tx = new RewardTransaction(
                UUID.randomUUID().toString(),
                customerId,
                points,
                RewardTransaction.TransactionType.REDEEM
        );
        return walletRepository.saveTransaction(tx);
    }
}
