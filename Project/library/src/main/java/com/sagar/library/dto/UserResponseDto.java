package com.sagar.library.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserResponseDto {
    private long id;
    private String name;
    private String email;
    private Set<String> roles;


}
