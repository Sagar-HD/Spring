package com.sagar.SpringJdbc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Student {
    @NotNull
    private Long id;
    @NotNull
    @NotEmpty
    private String name;
    @Min(value = 18)
    private int age;

    @Email
    private String email;
    @NotNull
    @NotEmpty
    private String subject;

 @NotNull
 @Min(value = 1)
    private int rollno;
}
