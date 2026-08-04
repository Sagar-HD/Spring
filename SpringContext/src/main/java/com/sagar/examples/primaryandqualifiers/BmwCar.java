package com.sagar.examples.primaryandqualifiers;

import com.sagar.examples.primaryandqualifiers.engines.Engine;
import org.springframework.stereotype.Component;

@Component
public class BmwCar {
    private Engine engine;
    public BmwCar(Engine engine) {
        this.engine = engine;
    }
    public void start(){
        engine.start();
    }
}
