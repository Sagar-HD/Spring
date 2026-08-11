package com.sagar.ticketbooking.exception;

public class MovieAlreadyStartedException extends BusinessConflictException {

    public MovieAlreadyStartedException(String message) {
        super(message);
    }
}
