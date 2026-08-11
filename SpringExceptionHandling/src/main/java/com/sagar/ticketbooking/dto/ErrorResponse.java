package com.sagar.ticketbooking.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private String path;
}
