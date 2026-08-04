package com.sagar.examples.primaryandqualifiers.engines;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class DeiselEngine implements Engine {
    public DeiselEngine(){
        System.out.println("Deisel engine created");
    }

    @Override
    public void start() {
        System.out.println("starting diesel engine");

    }
}
