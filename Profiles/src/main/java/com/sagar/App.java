package com.sagar;

import com.sagar.config.AppConfig;
import com.sagar.controller.PaymentContoller;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {
    public static void main(String[] args) {
        ApplicationContext context= new AnnotationConfigApplicationContext(AppConfig.class);
        PaymentContoller controller=context.getBean(PaymentContoller.class);
        controller.payment();
    }
}
