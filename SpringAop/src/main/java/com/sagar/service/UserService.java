package com.sagar.service;

import com.sagar.annotation.MeasureTime;
import com.sagar.dto.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {


    @MeasureTime(name = "createUser", warningTime = 1000)
    public User createUser(User user){
        System.out.println("user created");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
        return user;

    }

    public User getUser(User user){
        System.out.println("user fetched");
        return user;
    }
    public User selfInvocationGet(User user){
       return getUser(user);
    }

    public void throwException(){
        throw new RuntimeException("Exception");
    }

}
