package com.sagar.controller;

import com.sagar.dto.UserRequestDto;
import com.sagar.dto.UserResponseDto;
import com.sagar.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RestController
@Validated
@RequestMapping("/api/v1/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(201).body(userService.createUser(userRequestDto));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@Valid @PathVariable @Positive Long id) {
        UserResponseDto res=userService.getUserById(id);
        if(res==null){
                return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponseDto> deleteUserById(@Valid @PathVariable @Positive Long id) {
        if(userService.deleteUserById(id)){
            return ResponseEntity.status(204).build();
        }
        else{
            return ResponseEntity.notFound().build();
        }

    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUserById(@Valid @PathVariable @Positive Long id, @Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto res= userService.updateById(id, userRequestDto);
        if(res!=null){
            return ResponseEntity.ok(res);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }
}
