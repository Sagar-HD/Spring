package com.sagar.jwtsecurity.service;

import com.sagar.jwtsecurity.dto.LoginRequestDto;
import com.sagar.jwtsecurity.dto.LoginResponseDto;
import com.sagar.jwtsecurity.dto.RegisterRequestDto;
import com.sagar.jwtsecurity.entity.Role;
import com.sagar.jwtsecurity.entity.User;
import com.sagar.jwtsecurity.repository.RoleRepository;
import com.sagar.jwtsecurity.repository.UserRepository;
import com.sagar.jwtsecurity.util.JwtUtil;
import jakarta.transaction.Transactional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public class AuthService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,RoleRepository roleRepository,PasswordEncoder passwordEncoder,AuthenticationManager authenticationManager,JwtUtil jwtUtil){
        this.roleRepository=roleRepository;
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.authenticationManager=authenticationManager;
        this.jwtUtil=jwtUtil;
    }



    public void register(RegisterRequestDto registerRequestDto){
        User user=new User();
        user.setUsername(registerRequestDto.getUsername());
        String encodedPass=passwordEncoder.encode(registerRequestDto.getPassword());
        user.setPassword(encodedPass);
        Role role=roleRepository.findByName("USER").orElseThrow(()->new RuntimeException("Role not found"));
        user.getRoles().add(role);
        userRepository.save(user);

    }



    public LoginResponseDto login(LoginRequestDto loginRequestDto){
       Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(),loginRequestDto.getPassword()));

      return new LoginResponseDto(jwtUtil.generateToken(authentication));

    }
}
