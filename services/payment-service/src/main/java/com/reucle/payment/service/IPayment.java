package com.reucle.payment.service;

import com.reucle.payment.model.PaymentRecord;
import com.reucle.payment.model.Receipt;

// PM2: payment-service Class Diagram -> IPayment
public interface IPayment {
    Receipt processPayment(String userId, double amount);
    PaymentRecord refundPayment(String paymentId);
}
