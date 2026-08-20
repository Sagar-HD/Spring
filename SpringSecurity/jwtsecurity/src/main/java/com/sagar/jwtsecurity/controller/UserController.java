package com.sagar.jwtsecurity.controller;

import com.sagar.jwtsecurity.entity.User;
import com.sagar.jwtsecurity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService){
        this.userService=userService;
    }


    @GetMapping
    public ResponseEntity<User> getUser(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(userService.getUser(userDetails.getUsername()));

    }

    @DeleteMapping("/{username}")
    public String deleteUser(@PathVariable("username") String username){
        userService.deleteUser(username);
        return "user deleted";

    }



}
