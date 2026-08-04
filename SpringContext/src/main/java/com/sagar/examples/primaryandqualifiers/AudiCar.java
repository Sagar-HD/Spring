package com.sagar.examples.primaryandqualifiers;

import com.sagar.examples.primaryandqualifiers.engines.Engine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AudiCar {
    private final Engine engine;


    public AudiCar(@Qualifier("petrolEngine") Engine engine) {
        this.engine = engine;
    }
    public void start(){
        engine.start();
    }
}
