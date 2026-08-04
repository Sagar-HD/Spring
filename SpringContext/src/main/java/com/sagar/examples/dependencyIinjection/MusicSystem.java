package com.sagar.examples.dependencyIinjection;

import org.springframework.stereotype.Component;

@Component
public class MusicSystem {
    public MusicSystem(){
        System.out.println("music system created");
    }
}
