package com.sagar.ticketbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationFieldError {


        private String field;

        private String message;

}
