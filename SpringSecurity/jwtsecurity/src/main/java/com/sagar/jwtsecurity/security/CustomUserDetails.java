package com.sagar.jwtsecurity.security;

import com.sagar.jwtsecurity.entity.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private User targetUser;
    public  CustomUserDetails(User targetUser){
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
        return targetUser.getUsername();
    }
}
