package com.sagar.FiltersAndInterceptors.controller;

import com.sagar.FiltersAndInterceptors.dto.User;
import com.sagar.FiltersAndInterceptors.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService service;

    public UserController(UserService service){
        this.service=service;
    }

    @PostMapping
    public ResponseEntity<User> saveUser(@RequestBody User user){
        return service.saveUser(user);
    }
}
