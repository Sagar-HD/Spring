package com.sagar.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    @Size(min = 6 ,message = "minimum length of password must be 6")
    private String password;
    private String email;

}
