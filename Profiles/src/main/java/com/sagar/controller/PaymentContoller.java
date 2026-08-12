package com.sagar.controller;

import com.sagar.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class PaymentContoller {

    private final PaymentService paymentService;
    @Value("${server.app.mode}")
    private String mode;

    public PaymentContoller(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    public void payment() {
        System.out.println("running in mode : "+mode);
        paymentService.pay();

    }
}
