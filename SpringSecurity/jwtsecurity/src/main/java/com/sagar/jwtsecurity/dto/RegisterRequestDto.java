package com.sagar.jwtsecurity.dto;

import lombok.Data;

@Data
public class RegisterRequestDto {
    private String username;
    private String password;
}
