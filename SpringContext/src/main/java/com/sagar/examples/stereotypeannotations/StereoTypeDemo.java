package com.sagar.examples.stereotypeannotations;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import  com.sagar.examples.stereotypeannotations.config.MyConfig;
public class StereoTypeDemo {
    public static void main(String[] args) {
        ApplicationContext context=new AnnotationConfigApplicationContext(MyConfig.class);

    }
}
