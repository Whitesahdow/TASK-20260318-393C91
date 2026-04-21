package com.busapp.controller;

import com.busapp.service.DuplicateException;
import com.busapp.service.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateException(DuplicateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", exception.getMessage()));
    }
}
