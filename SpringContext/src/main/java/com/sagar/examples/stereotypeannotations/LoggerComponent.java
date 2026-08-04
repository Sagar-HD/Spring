package com.sagar.examples.stereotypeannotations;

import org.springframework.stereotype.Component;

@Component
public class LoggerComponent {
    public LoggerComponent(){
        System.out.println("logger component created");
    }
}
