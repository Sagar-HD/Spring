package com.sagar.ticketbooking.dto;

import lombok.Data;
import org.springframework.validation.FieldError;

import java.util.List;

@Data
public class ValidationErrorResponse {
    private String message;
    private String path;
    private List<ValidationFieldError> fieldErrors;
}
