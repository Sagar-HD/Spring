package com.sagar.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDto {

    private Long id;

    private String name;

    private String email;

    private String message;

    public UserResponseDto(UserRequestDto req){

        this.name = req.getName();
        this.email = req.getEmail();

    }

}