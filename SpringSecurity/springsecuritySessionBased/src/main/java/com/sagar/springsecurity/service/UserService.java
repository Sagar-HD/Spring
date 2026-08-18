package com.sagar.springsecurity.service;

import com.sagar.springsecurity.entity.Role;
import com.sagar.springsecurity.entity.User;
import com.sagar.springsecurity.repository.RoleRepository;
import com.sagar.springsecurity.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(User user){
        Role userRole=roleRepository.findById(1L).get();

        user.setRoles(new HashSet<>(List.of(userRole)));
        user.setIsEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean toggleUserVisibility(Long id){
        User user=userRepository.findById(id).orElseThrow(()->new IllegalArgumentException("user not found"));
        user.setIsEnabled(!user.getIsEnabled());
        return user.getIsEnabled();
    }


}
