package com.sagar.examples.stereotypeannotations;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    LoggerComponent loggerComponent;

    public  UserRepository(){
        System.out.println("repository created");
    }

    public void setLoggerComponent(LoggerComponent logger){
        System.out.println("adding logger to repo");
        this.loggerComponent=logger;
    }
}
