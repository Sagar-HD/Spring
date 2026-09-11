package com.sagar.library.service;

import com.sagar.library.dto.UserRegistrationRequestDto;
import com.sagar.library.dto.UserResponseDto;
import com.sagar.library.entity.Role;
import com.sagar.library.entity.User;
import com.sagar.library.repository.RoleRepository;
import com.sagar.library.repository.UserRepository;
import com.sagar.library.security.CustomUserDetails;
import com.sagar.library.util.EmailUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private EmailUtil emailUtil;

    public UserService(UserRepository userRepository,RoleRepository roleRepository,PasswordEncoder passwordEncoder,EmailUtil emailUtil) {

        this.userRepository = userRepository;
        this.roleRepository=roleRepository;
        this.passwordEncoder=passwordEncoder;
        this.emailUtil=emailUtil;
    }

    public UserResponseDto getUser(UserDetails user){
        User userResp= userRepository.findByName(user.getUsername()).orElseThrow(()->new UsernameNotFoundException("user not found"));
        UserResponseDto userResponseDto=new UserResponseDto();
        userResponseDto.setEmail(userResp.getEmail());
        userResponseDto.setName(userResp.getName());
        userResponseDto.setId(userResp.getId());
        userResponseDto.setRoles(userResp.getRoles().stream().map((role)->role.getName()).collect(Collectors.toSet()));
        return userResponseDto;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public List<UserResponseDto> getAllUsers(){
       List<User> users= userRepository.findAll();
       List<UserResponseDto> usersResp=users.stream().map((user)->UserResponseDto.builder().id(user.getId()).name(user.getName()).email(user.getEmail()).build()).toList();
       return usersResp;
    }


    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto addLibrarian(UserRegistrationRequestDto userRegistrationRequestDto){
        User newLibrarian=new User();
        Role role=roleRepository.findByName("LIBRARIAN").orElseThrow(()->new IllegalArgumentException("role not found"));
        newLibrarian.setName(userRegistrationRequestDto.getName());
        newLibrarian.setEmail(userRegistrationRequestDto.getEmail());
        newLibrarian.getRoles().add(role);
        newLibrarian.setPassword(passwordEncoder.encode(userRegistrationRequestDto.getPassword()));
        userRepository.save(newLibrarian);
        CustomUserDetails userDetails=(CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("librarian id : {} added by admin id : {}",newLibrarian.getId(),userDetails.getUserId());

        emailUtil.sendMail(newLibrarian.getEmail(),"Congratulations ! you were added as an librarian","You have been successfully registered to our library app as an Librarian you can Login with credentials ");

        return UserResponseDto.builder().name(newLibrarian.getName()).id(newLibrarian.getId()).email(newLibrarian.getEmail()).build();

    }

}
