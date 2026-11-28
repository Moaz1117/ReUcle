package com.reucle.reward.service;

import com.reucle.reward.model.RewardTransaction;
import com.reucle.reward.model.Wallet;
import com.reucle.reward.repository.IWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// PM2 Test Map: reward-service US38 Collaboration & Sequence Diagram
@ExtendWith(MockitoExtension.class)
class RewardManagerTest {

    @Mock
    private IWalletRepository walletRepository;

    private RewardManager rewardManager;

    @BeforeEach
    void setUp() {
        rewardManager = new RewardManager(walletRepository, new RewardCalculator());
        when(walletRepository.saveWallet(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletRepository.saveTransaction(any(RewardTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("[US38 Collaboration/Sequence] Existing wallet -> credits calculated points and saves transaction")
    void creditCustomerReward_existingWallet_creditsPointsAndPersistsTransaction() {
        Wallet existing = new Wallet("w-1", "cust-1", 100.0);
        when(walletRepository.findByCustomerId("cust-1")).thenReturn(Optional.of(existing));

        RewardTransaction tx = rewardManager.creditCustomerReward("cust-1", 5.0, 10.0);

        assertEquals(50.0, tx.getPoints());
        assertEquals(150.0, existing.getBalance());
        assertEquals(RewardTransaction.TransactionType.CREDIT, tx.getType());
        verify(walletRepository).saveWallet(existing);
        verify(walletRepository).saveTransaction(any(RewardTransaction.class));
    }

    @Test
    @DisplayName("[US38 Collaboration/Sequence] New customer -> creates wallet with zero base and credits points")
    void creditCustomerReward_newCustomer_createsWalletAndCreditsPoints() {
        when(walletRepository.findByCustomerId("cust-2")).thenReturn(Optional.empty());

        RewardTransaction tx = rewardManager.creditCustomerReward("cust-2", 2.5, 20.0);

        assertEquals(50.0, tx.getPoints());
        verify(walletRepository).saveWallet(argThat(w -> w.getCustomerId().equals("cust-2") && w.getBalance() == 50.0));
        verify(walletRepository).saveTransaction(tx);
    }
}
