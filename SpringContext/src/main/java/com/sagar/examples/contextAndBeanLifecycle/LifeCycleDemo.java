package com.sagar.examples.contextAndBeanLifecycle;

import com.sagar.examples.contextAndBeanLifecycle.config.AppConfig;
import org.springframework.context.ApplicationContext;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class LifeCycleDemo {
    public static void main(String[] args) {
        System.out.println("Hey welcome to birth and death of ioc container and beans");
        AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext();
        System.out.println("application context and bean factory created");
        context.register(AppConfig.class);
        System.out.println("now ill call refresh method");
        System.out.println();
        System.out.println(" by calling refresh method using config meta data ill start scanning all compoenents ");
        System.out.println();
        context.refresh();
        context.close();
        System.out.println("application context and bean factory closed");
    }
}
