package com.reucle.payment.service;

import com.reucle.payment.gateway.IPaymentGW;
import com.reucle.payment.model.PaymentRecord;
import com.reucle.payment.model.Receipt;
import com.reucle.payment.repository.IPaymentRepository;
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

// PM2 Test Map: payment-service Class Diagram & PaymentRecord State Diagram
@ExtendWith(MockitoExtension.class)
class PaymentRecordAndManagerTest {

    @Mock
    private IPaymentRepository repository;

    @Mock
    private IPaymentGW gateway;

    private PaymentManager manager;

    @BeforeEach
    void setUp() {
        manager = new PaymentManager(repository, gateway);
    }

    @Test
    @DisplayName("[Payment State: PENDING->PAID] Gateway approves -> record becomes PAID and Receipt is returned")
    void processPayment_gatewayApproves_transitionsToPaidAndIssuesReceipt() {
        when(repository.save(any(PaymentRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gateway.charge("u-1", 40.0)).thenReturn(true);

        Receipt receipt = manager.processPayment("u-1", 40.0);

        assertNotNull(receipt.receiptId());
        assertEquals(40.0, receipt.amount());
        verify(repository, times(2)).save(any(PaymentRecord.class));
        verify(repository, atLeastOnce()).save(argThat(r -> r.getStatus() == PaymentRecord.PaymentStatus.PAID));
    }

    @Test
    @DisplayName("[Payment State: PENDING->FAILED] Gateway declines -> record transitions to FAILED")
    void processPayment_gatewayDeclines_transitionsToFailed() {
        when(repository.save(any(PaymentRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gateway.charge("u-1", 40.0)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> manager.processPayment("u-1", 40.0));
        verify(repository, times(2)).save(any(PaymentRecord.class));
        verify(repository, atLeastOnce()).save(argThat(r -> r.getStatus() == PaymentRecord.PaymentStatus.FAILED));
    }

    @Test
    @DisplayName("[Payment State: PAID->REFUNDED] Refunding PAID record -> transitions to REFUNDED")
    void refundPayment_paidRecord_transitionsToRefunded() {
        when(repository.save(any(PaymentRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        PaymentRecord paidRecord = new PaymentRecord("pay-1", "u-1", 40.0);
        paidRecord.markPaid();
        when(repository.findById("pay-1")).thenReturn(Optional.of(paidRecord));
        when(gateway.refund("pay-1", 40.0)).thenReturn(true);

        PaymentRecord result = manager.refundPayment("pay-1");

        assertEquals(PaymentRecord.PaymentStatus.REFUNDED, result.getStatus());
        verify(repository).save(paidRecord);
    }

    @Test
    @DisplayName("[Payment State Guard] Refunding non-PAID record -> throws IllegalStateException")
    void refund_pendingRecord_throwsIllegalStateException() {
        PaymentRecord pendingRecord = new PaymentRecord("pay-2", "u-1", 20.0);
        assertThrows(IllegalStateException.class, pendingRecord::refund);
        verifyNoInteractions(repository, gateway);
    }
}
