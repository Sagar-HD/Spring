package com.sagar.library.exception;

public class BookNotAvailableException extends BusinessException{
    public BookNotAvailableException(String message) {
        super(message);
    }
}
