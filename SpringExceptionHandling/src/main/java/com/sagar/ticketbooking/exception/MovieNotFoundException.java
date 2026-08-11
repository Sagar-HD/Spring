package com.sagar.ticketbooking.exception;

public class MovieNotFoundException extends BusinessException {

    public MovieNotFoundException(String message) {
        super(message);
    }
}
