package com.sagar.library.dto;

import lombok.Data;

@Data
public class ErrorResponseDto {
    private String path;
    private String message;
}
