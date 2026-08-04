package com.sagar.examples.primaryandqualifiers;

import com.sagar.examples.primaryandqualifiers.config.MyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class PrimaryAndQualifierDemo {
    public static void main(String[] args) {
        ApplicationContext context=new AnnotationConfigApplicationContext(MyConfig.class);
        BmwCar car=context.getBean(BmwCar.class);
        //Primary engine diesel
        car.start();
        AudiCar audiCar=context.getBean(AudiCar.class);
        //Qualifier engine petrol
        audiCar.start();
    }
}
