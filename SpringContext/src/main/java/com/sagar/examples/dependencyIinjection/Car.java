package com.sagar.examples.dependencyIinjection;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class Car {

    //we can make final in constructor injection but not in setter and field
    private final Engine engine;

    @Autowired
    private MusicSystem musicSystem;

    @Autowired
    @Setter
    private TurboAddOn turboAddOn;

    public Car(Engine engine){
        System.out.println("car created");
        this.engine=engine;
    }


}
