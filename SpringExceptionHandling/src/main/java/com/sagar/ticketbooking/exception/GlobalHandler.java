package com.sagar.ticketbooking.exception;

import com.sagar.ticketbooking.dto.ErrorResponse;
import com.sagar.ticketbooking.dto.ValidationErrorResponse;
import com.sagar.ticketbooking.dto.ValidationFieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;

@RestControllerAdvice(basePackages = "com.sagar.ticketbooking.controller")
public class GlobalHandler {

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ErrorResponse> movieNotFound(MovieNotFoundException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getRequestURI());
        System.out.println("warn : movie not found exception occurred");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public  ResponseEntity<ErrorResponse> genricRuntimeException(RuntimeException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getRequestURI());
        System.err.println("unknown runtime exception occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // handler for path and query param validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        ValidationErrorResponse res=new ValidationErrorResponse();
        res.setMessage("validation failed");
        res.setPath(request.getRequestURI());
        System.out.println("warn : path or query param validation failed");


        return ResponseEntity.badRequest().body(res);
    }
    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<ErrorResponse> conflicts(BusinessConflictException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getRequestURI());
        System.out.println("warn : business conflict exception occurred");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> validations(MethodArgumentNotValidException ex, HttpServletRequest request){
        ValidationErrorResponse res=new ValidationErrorResponse();
        res.setMessage("validation failed");
        res.setPath(request.getRequestURI());
        res.setFieldErrors(ex.getFieldErrors().stream().map(fieldError -> new ValidationFieldError(fieldError.getField(), fieldError.getDefaultMessage())).toList());
        System.out.println("warn : request body validation failed");
        return ResponseEntity.badRequest().body(res);
    }

}
