package com.sagar.contoller;

import com.sagar.dto.User;
import com.sagar.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aop")
public class UserController {

    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        return ResponseEntity.ok(userService.createUser(user));
    }
    @GetMapping
    public ResponseEntity<User> getUser(@RequestBody User user){
        return ResponseEntity.ok(userService.getUser(user));
    }

    @GetMapping("/self")
    public ResponseEntity<User> getUserSelf(@RequestBody User user){
        return ResponseEntity.ok(userService.selfInvocationGet(user));
    }

    @GetMapping("/exception")
    public ResponseEntity<User> throwException(){
        userService.throwException();
        return ResponseEntity.ok().build();
    }
}
