package com.sagar.library.exception;

public class BookLoanNotClearedBeforeDeleteException extends BusinessException{
    public BookLoanNotClearedBeforeDeleteException(String message) {
        super(message);
    }
}
