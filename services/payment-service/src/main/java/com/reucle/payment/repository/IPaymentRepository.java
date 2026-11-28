package com.reucle.payment.repository;

import com.reucle.payment.model.PaymentRecord;
import java.util.Optional;

// PM2: payment-service Class Diagram -> IPaymentRepository
public interface IPaymentRepository {
    Optional<PaymentRecord> findById(String paymentId);
    PaymentRecord save(PaymentRecord record);
}
