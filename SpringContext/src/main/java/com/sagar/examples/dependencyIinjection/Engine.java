package com.sagar.examples.dependencyIinjection;

import org.springframework.stereotype.Component;

@Component
public class Engine {
    public Engine(){
        System.out.println("engine created");
    }
}
