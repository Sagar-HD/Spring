package com.sagar.jwtsecurity.service;

import com.sagar.jwtsecurity.entity.User;
import com.sagar.jwtsecurity.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;

@Component
public class UserService {

    private UserRepository userRepository;


    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public User getUser(String username){
        return  userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("user not found"));
    }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
    public void deleteUser(String username){
        long deleted=userRepository.deleteByUsername(username);
        if(deleted==0){
            throw  new RuntimeException("user not found");

        }
    }


}
