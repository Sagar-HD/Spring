package com.sagar.library.controller;

import com.sagar.library.dto.UserRegistrationRequestDto;
import com.sagar.library.dto.UserResponseDto;
import com.sagar.library.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(userService.getUser(user));

    }
    @PostMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @PostMapping("/librarian")
    public ResponseEntity<UserResponseDto> addLibrarian(@RequestBody UserRegistrationRequestDto userRegistrationRequestDto){
        return ResponseEntity.ok(userService.addLibrarian(userRegistrationRequestDto));
    }
}
