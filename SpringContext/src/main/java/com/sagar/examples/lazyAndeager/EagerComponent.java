package com.sagar.examples.lazyAndeager;

import org.springframework.stereotype.Component;

@Component
public class EagerComponent {
    public  EagerComponent(){
        System.out.println("Eager component created");
    }
}
