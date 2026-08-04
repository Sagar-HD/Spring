package com.sagar.examples.primaryandqualifiers.engines;

import org.springframework.stereotype.Component;

@Component
public class PetrolEngine implements  Engine{
    public PetrolEngine(){
        System.out.println("Petrol engine created");
    }

    @Override
    public void start() {
        System.out.println("starting petrol engine");
    }
}
