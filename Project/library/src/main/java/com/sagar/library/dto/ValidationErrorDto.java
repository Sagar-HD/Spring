package com.sagar.library.dto;

import lombok.Data;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ValidationErrorDto {
    private String path;
    private String message;
    Map<String,String> errors;
}
