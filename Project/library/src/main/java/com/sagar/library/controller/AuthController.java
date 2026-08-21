package com.sagar.library.controller;

import com.sagar.library.dto.UserLoginRequestDto;
import com.sagar.library.dto.UserLoginResponseDto;
import com.sagar.library.dto.UserRegistrationRequestDto;
import com.sagar.library.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
     private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody @Valid UserRegistrationRequestDto userRegistrationRequestDto){

        authService.registerUser(userRegistrationRequestDto);
        return "User Registered";

    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> loginUser(@RequestBody @Valid  UserLoginRequestDto userLoginRequestDto){
        UserLoginResponseDto userLoginResponseDto=new UserLoginResponseDto();

       userLoginResponseDto.setToken(authService.loginUser(userLoginRequestDto));
       return ResponseEntity.ok(userLoginResponseDto);
    }
}
