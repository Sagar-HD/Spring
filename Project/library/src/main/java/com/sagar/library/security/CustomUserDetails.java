package com.sagar.library.security;

import com.sagar.library.entity.User;
import com.sagar.library.repository.UserRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


public class CustomUserDetails implements UserDetails {
    private User targetUser;


    public CustomUserDetails(User targetUser){
        this.targetUser=targetUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return targetUser.getRoles().stream().map((role)->new SimpleGrantedAuthority("ROLE_"+role.getName())).toList();

    }

    @Override
    public @Nullable String getPassword() {
        return targetUser.getPassword();
    }

    @Override
    public String getUsername() {
        return targetUser.getName();
    }

    public Long getUserId() {
        return targetUser.getId();
    }

    public String getUserEmail(){return targetUser.getEmail();}
    public Set<String> getRoles(){return targetUser.getRoles().stream().map((role)->role.getName()).collect(Collectors.toSet());}
}
