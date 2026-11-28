package com.reucle.payment.service;

import com.reucle.payment.gateway.IPaymentGW;
import com.reucle.payment.model.PaymentRecord;
import com.reucle.payment.model.Receipt;
import com.reucle.payment.repository.IPaymentRepository;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

// PM2: payment-service Class & State Diagrams -> PaymentManager
public class PaymentManager implements IPayment {
    private final IPaymentRepository paymentRepository;
    private final IPaymentGW paymentGateway;

    public PaymentManager(IPaymentRepository paymentRepository, IPaymentGW paymentGateway) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
    }

    @Override
    public Receipt processPayment(String userId, double amount) {
        PaymentRecord record = new PaymentRecord(UUID.randomUUID().toString(), userId, amount);
        paymentRepository.save(record);

        boolean charged = paymentGateway.charge(userId, amount);
        if (!charged) {
            record.markFailed();
            paymentRepository.save(record);
            throw new IllegalStateException("Gateway charge failed");
        }

        record.markPaid();
        paymentRepository.save(record);
        return new Receipt(UUID.randomUUID().toString(), record.getPaymentId(), amount, Instant.now());
    }

    @Override
    public PaymentRecord refundPayment(String paymentId) {
        PaymentRecord record = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found: " + paymentId));

        boolean refunded = paymentGateway.refund(paymentId, record.getAmount());
        if (!refunded) {
            throw new IllegalStateException("Gateway refund failed");
        }

        record.refund();
        return paymentRepository.save(record);
    }
}
