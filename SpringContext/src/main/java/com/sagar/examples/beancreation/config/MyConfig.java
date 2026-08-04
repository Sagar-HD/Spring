package com.sagar.examples.beancreation.config;

import com.sagar.examples.beancreation.SpecialPerson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan("com.sagar.examples.beancreation")
public class MyConfig{

    @Bean
    public SpecialPerson sagar() {
        return new SpecialPerson("Sagar", 18);
    }


    @Bean
    public SpecialPerson god() {
        return new SpecialPerson("god", Integer.MAX_VALUE);
    }

}
