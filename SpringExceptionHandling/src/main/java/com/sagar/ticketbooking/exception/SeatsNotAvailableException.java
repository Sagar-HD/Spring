package com.sagar.ticketbooking.exception;

public class SeatsNotAvailableException extends  BusinessConflictException{
    public SeatsNotAvailableException(String message) {
        super(message);
    }
}
