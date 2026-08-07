package com.sagar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentService {
    private String paymentName;
    @Value("${payment.noOfRetries}")
    private int noOfRetries;

    public PaymentService(@Value("${payment.name}") String paymentName) {
        this.paymentName = paymentName;
    }

    public void display() {
        System.out.println("Payment Name: " + paymentName);
        System.out.println("No of Retries: " + noOfRetries);
    }
}
