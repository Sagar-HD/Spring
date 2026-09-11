package com.sagar.library.service;

import com.sagar.library.annotation.MeasureTime;
import com.sagar.library.dto.UserLoginRequestDto;
import com.sagar.library.dto.UserLoginResponseDto;
import com.sagar.library.dto.UserRegistrationRequestDto;
import com.sagar.library.dto.UserResponseDto;
import com.sagar.library.entity.Role;
import com.sagar.library.entity.User;
import com.sagar.library.repository.RoleRepository;
import com.sagar.library.repository.UserRepository;
import com.sagar.library.security.CustomUserDetails;
import com.sagar.library.util.EmailUtil;
import com.sagar.library.util.JwtUtil;
import io.jsonwebtoken.Jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private RoleRepository roleRepository;
    private EmailUtil emailUtil;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil,RoleRepository roleRepository,EmailUtil emailUtil){
        this.userRepository=userRepository;
        this.authenticationManager=authenticationManager;
        this.jwtUtil=jwtUtil;
        this.roleRepository=roleRepository;
        this.passwordEncoder=passwordEncoder;
        this.emailUtil=emailUtil;
    }

    @MeasureTime
    public void registerUser(UserRegistrationRequestDto userRegistrationRequestDto){
        User newUser=new User();
        newUser.setEmail(userRegistrationRequestDto.getEmail());
        newUser.setName(userRegistrationRequestDto.getName());
        newUser.setPassword(passwordEncoder.encode(userRegistrationRequestDto.getPassword()));
        Role role=roleRepository.findByName("USER").orElseThrow(()->new RuntimeException("role not found"));
        newUser.getRoles().add(role);
        userRepository.save(newUser);
        log.info("User registered with user id : {}",newUser.getId());
        emailUtil.sendMail(newUser.getEmail(),"Thank you for registering in Library app","You have been successfully registered to our library app you can Login with credentials and start using our services");

    }

    public UserLoginResponseDto loginUser(UserLoginRequestDto userLoginRequestDto){
      UsernamePasswordAuthenticationToken token= new UsernamePasswordAuthenticationToken(userLoginRequestDto.getName(),userLoginRequestDto.getPassword());
     Authentication authentication=authenticationManager.authenticate(token);
     CustomUserDetails loggedInUser=(CustomUserDetails) authentication.getPrincipal();
        log.info("User Logged in  : {}",loggedInUser.getUserId());
     return new UserLoginResponseDto(jwtUtil.generateToken(authentication),new UserResponseDto(loggedInUser.getUserId(), loggedInUser.getUsername(), loggedInUser.getUserEmail(),loggedInUser.getRoles()));
    }


}
