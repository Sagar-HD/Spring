package com.sagar.examples.dependencyIinjection;

import com.sagar.examples.dependencyIinjection.config.MyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class DependencyInjectionDemo {
    public static void main(String[] args) {
        ApplicationContext context=new AnnotationConfigApplicationContext(MyConfig.class);

        Car car=context.getBean(Car.class);

    }
}
