package com.sagar.library.service;

import com.sagar.library.dto.UserLoginRequestDto;
import com.sagar.library.dto.UserRegistrationRequestDto;
import com.sagar.library.entity.Role;
import com.sagar.library.entity.User;
import com.sagar.library.repository.RoleRepository;
import com.sagar.library.repository.UserRepository;
import com.sagar.library.util.JwtUtil;
import io.jsonwebtoken.Jwt;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private RoleRepository roleRepository;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil,RoleRepository roleRepository){
        this.userRepository=userRepository;
        this.authenticationManager=authenticationManager;
        this.jwtUtil=jwtUtil;
        this.roleRepository=roleRepository;
        this.passwordEncoder=passwordEncoder;
    }

    public void registerUser(UserRegistrationRequestDto userRegistrationRequestDto){
        User newUser=new User();
        newUser.setEmail(userRegistrationRequestDto.getEmail());
        newUser.setName(userRegistrationRequestDto.getName());
        newUser.setPassword(passwordEncoder.encode(userRegistrationRequestDto.getPassword()));
        Role role=roleRepository.findByName("USER").orElseThrow(()->new RuntimeException("role not found"));
        newUser.getRoles().add(role);
        userRepository.save(newUser);

    }

    public String loginUser(UserLoginRequestDto userLoginRequestDto){
      UsernamePasswordAuthenticationToken token= new UsernamePasswordAuthenticationToken(userLoginRequestDto.getName(),userLoginRequestDto.getPassword());
     Authentication authentication=authenticationManager.authenticate(token);
     return jwtUtil.generateToken(authentication);
    }


}
