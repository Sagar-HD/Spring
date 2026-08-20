package com.sagar.jwtsecurity.controller;

import com.sagar.jwtsecurity.dto.LoginRequestDto;
import com.sagar.jwtsecurity.dto.LoginResponseDto;
import com.sagar.jwtsecurity.dto.RegisterRequestDto;

import com.sagar.jwtsecurity.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private AuthService authService;

    public AuthController(AuthService authService){
        this.authService=authService;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody RegisterRequestDto registerRequestDto){
        authService.register(registerRequestDto);
        return "user registered succusfully";
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){

        return ResponseEntity.ok(authService.login(loginRequestDto));
    }



}
