package com.reucle.reward.repository;

import com.reucle.reward.model.RewardTransaction;
import com.reucle.reward.model.Wallet;
import java.util.Optional;

// PM2: reward-service Class/Collaboration Diagram (US38) -> IWalletRepository
public interface IWalletRepository {
    Optional<Wallet> findByCustomerId(String customerId);
    Wallet saveWallet(Wallet wallet);
    RewardTransaction saveTransaction(RewardTransaction transaction);
}
