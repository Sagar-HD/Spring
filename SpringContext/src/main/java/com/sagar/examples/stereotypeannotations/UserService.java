package com.sagar.examples.stereotypeannotations;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepository userRepository;
    public UserService(@Qualifier("loggingRepository") UserRepository userRepository){
        this.userRepository=userRepository;
        System.out.println("user service created");
    }
}
