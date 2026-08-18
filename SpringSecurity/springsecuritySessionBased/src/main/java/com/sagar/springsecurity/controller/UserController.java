package com.sagar.springsecurity.controller;

import com.sagar.springsecurity.entity.User;
import com.sagar.springsecurity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/user")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public String registerUser(@RequestBody User user){
        userService.registerUser(user);
        return "User Saved";
    }

    @PostMapping("/toggleUser/{id}")
    public ResponseEntity<String> toggleUser(@PathVariable("id") Long id ){
        boolean visibility=userService.toggleUserVisibility(id);
        return visibility?ResponseEntity.ok("User Enabled"):ResponseEntity.ok("User Disabled");

    }

}
