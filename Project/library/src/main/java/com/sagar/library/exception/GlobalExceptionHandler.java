package com.sagar.library.exception;

import com.sagar.library.dto.ErrorResponseDto;
import com.sagar.library.dto.ValidationErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<ValidationErrorDto> validationExceptionHandler(MethodArgumentNotValidException validationException, HttpServletRequest req){
        ValidationErrorDto validationErrorDto=new ValidationErrorDto();
        validationErrorDto.setPath(req.getRequestURI());
        validationErrorDto.setMessage("invalid fileds");
        HashMap<String ,String> errors=new HashMap<>();
        validationException.getBindingResult().getFieldErrors().forEach((error)-> errors.put(error.getField(),error.getDefaultMessage()));
        validationErrorDto.setErrors(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorDto);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<String> authenticationExceptionHandler(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not authorized");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> generealExceptionHandler(Exception e, HttpServletRequest req){
        ErrorResponseDto errorResponseDto=new ErrorResponseDto();
        errorResponseDto.setPath(req.getRequestURI());
        errorResponseDto.setMessage("something went wrong");
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseDto);
    }
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> businessExceptionHandler(Exception e, HttpServletRequest req){
        ErrorResponseDto errorResponseDto=new ErrorResponseDto();
        errorResponseDto.setPath(req.getRequestURI());
        errorResponseDto.setMessage(e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public  ResponseEntity<ErrorResponseDto> badCredentialsHandler(Exception e, HttpServletRequest req){
        ErrorResponseDto errorResponseDto=new ErrorResponseDto();
        errorResponseDto.setPath(req.getRequestURI());
        errorResponseDto.setMessage("username or password wrong");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }


}
