package com.sagar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class OrderService implements ApplicationRunner {
    @Value("${deployer.name}")
    private String deployerName;

    private PaymentService paymentService;

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void display() {
        System.out.println("Deployer Name: " + deployerName);
        paymentService.display();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        display();

    }
}
