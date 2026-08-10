package com.sagar.dto;

import com.sagar.constants.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserRequestDto {
    //this message shows only if we have exception handler
    //if we don't have exception handler then it will show default message
    @NotBlank(message = "name cannot be blank ")
    @Size(min = 3, max = 20, message = "name should be between 3 and 20 characters")
    private String name;

    @NotNull
    @Min(value = 18, message = "age should be between 18 and 100")
    @Max(value = 100, message = "age should be between 18 and 100")
    private Integer age;

    @NotBlank(message = "email cannot be blank")
    @Email
    private String email;

    @NotBlank(message = "password cannot be blank")
    @Size(min = 8, max = 20, message = "password should be between 8 and 20 characters")
    private String password;

    @NotBlank(message = "phone cannot be blank")
    @Positive(message = "just checking positive validation")
    private String phone;

    @NotNull
    private Gender gender;

    @NotNull
    @Past
    private LocalDate dob;

    @NotEmpty(message = "skills cannot be empty")// checks not null +  size > 0
    @Size(min = 1, max = 6, message = "at least one skill is required and maximum 6 skills allowed")
    private List<@NotBlank(message = "skill cannot be blank") String> skills;

    @NotNull
    @Valid
    private  AddressDto address;

}
