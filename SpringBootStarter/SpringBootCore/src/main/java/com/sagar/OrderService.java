package com.sagar;

import org.springframework.stereotype.Component;

@Component
public class OrderService {
    private  PaymentService paymentService;
    public  OrderService(PaymentService paymentService){
        this.paymentService = paymentService;
        System.out.println("PaymentService is injected");
        System.out.println("OrderService is created");
    }
}
