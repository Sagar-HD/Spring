package com.sagar.examples.beancreation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class Person {
    private String name;
    private int age;

    public Person(){
        System.out.println("person created");
    }


}
