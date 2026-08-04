package com.sagar.examples.contextAndBeanLifecycle;

import lombok.Setter;
import org.springframework.stereotype.Component;

@Setter
@Component
public class PaymentService {

    private String someUsefulResource;

    public PaymentService() {
        System.out.println("PaymentService bean is created");
        System.out.println();
    }




}
