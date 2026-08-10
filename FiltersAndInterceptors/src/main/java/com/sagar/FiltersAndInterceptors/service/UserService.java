package com.sagar.FiltersAndInterceptors.service;

import com.sagar.FiltersAndInterceptors.dto.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public ResponseEntity<User> saveUser(User user){
        System.out.println("user saved");
        return ResponseEntity.ok(user);
    }

}
