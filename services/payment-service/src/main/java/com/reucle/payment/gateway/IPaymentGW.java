package com.reucle.payment.gateway;

// PM2: payment-service Class Diagram -> IPaymentGW
public interface IPaymentGW {
    boolean charge(String userId, double amount);
    boolean refund(String paymentId, double amount);
}
