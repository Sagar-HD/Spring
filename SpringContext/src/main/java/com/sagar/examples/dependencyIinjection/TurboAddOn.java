package com.sagar.examples.dependencyIinjection;

import org.springframework.stereotype.Component;

@Component
public class TurboAddOn {
    public TurboAddOn(){
        System.out.println("turbo created");
    }
}
