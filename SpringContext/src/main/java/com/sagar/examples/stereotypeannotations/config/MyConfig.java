package com.sagar.examples.stereotypeannotations.config;

import com.sagar.examples.stereotypeannotations.LoggerComponent;
import com.sagar.examples.stereotypeannotations.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan("com.sagar.examples.stereotypeannotations")
public class MyConfig {


    //Di in bean
    //we have 2 beans of repository 1 from @repository and 1 from bean
    @Bean
    public UserRepository loggingRepository(LoggerComponent logger){
        UserRepository loggerRepo= new UserRepository();
        loggerRepo.setLoggerComponent(logger);
        return loggerRepo;

    }

}
