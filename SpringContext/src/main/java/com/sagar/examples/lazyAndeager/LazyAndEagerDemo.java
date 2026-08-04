package com.sagar.examples.lazyAndeager;

import com.sagar.examples.lazyAndeager.config.MyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class LazyAndEagerDemo {
    public static void main(String[] args) {
        ApplicationContext context=new AnnotationConfigApplicationContext(MyConfig.class);
        System.out.println("apllication context is loaded");

        System.out.println("lazy component not created yet");

       //using lazy component method in lazy dependent component could have also caused creation of lazy

        LazyComponent lazy=context.getBean(LazyComponent.class);




    }
}
