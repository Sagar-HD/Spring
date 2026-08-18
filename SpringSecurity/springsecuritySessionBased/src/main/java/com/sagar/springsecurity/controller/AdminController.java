package com.sagar.springsecurity.controller;

import com.sagar.springsecurity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {

    @GetMapping
    public String admin(){
        return "U HAVE ADMIN ACCESS";
    }

}
