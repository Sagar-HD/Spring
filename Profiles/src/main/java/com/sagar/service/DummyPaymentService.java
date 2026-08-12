package com.sagar.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev","default"})
public class DummyPaymentService implements PaymentService{
    @Override
    public void pay() {
        System.out.println("Payed using Dummy");
    }
}
