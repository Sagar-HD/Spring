package com.sagar.examples.contextAndBeanLifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class OrderService implements BeanNameAware, ApplicationContextAware {
    private PaymentService paymentService;
    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
        System.out.println("OrderService bean is created with its dependency injected ");
        System.out.println();
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("bean aware method is called in case i need to store bean name info");

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("bean aware method is called in case i need to store application context info");
        System.out.println();
    }

    @PostConstruct
    public void init() {
        System.out.println("post construct method is called");
        paymentService.setSomeUsefulResource("any useful resource");
        System.out.println("setting usefull resource to dependency or bean itself after initialisation before using");
        System.out.println();
        System.out.println("now bean are eligible for using finally !!");
        System.out.println();
    }
    @PreDestroy
    public void destroy() {
        paymentService.setSomeUsefulResource(null);
        System.out.println("pre destroy method is called");
        System.out.println("clearing some resources before getting destory so gc can collect");
        System.out.println("now bean is going to destroy");
        System.out.println();
    }
}
