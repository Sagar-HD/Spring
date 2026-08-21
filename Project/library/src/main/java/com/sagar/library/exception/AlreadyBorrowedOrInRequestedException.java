package com.sagar.library.exception;

public class AlreadyBorrowedOrInRequestedException extends BusinessException {
    public AlreadyBorrowedOrInRequestedException(String message) {
        super(message);
    }
}
