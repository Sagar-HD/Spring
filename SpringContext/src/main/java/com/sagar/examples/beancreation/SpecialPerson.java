package com.sagar.examples.beancreation;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class SpecialPerson {
    private String name;
    private int age;

    public SpecialPerson(String name, int age) {
        this.name = name;
        this.age = age;
        System.out.println("special person created" +name +" : "+age);
    }
}
