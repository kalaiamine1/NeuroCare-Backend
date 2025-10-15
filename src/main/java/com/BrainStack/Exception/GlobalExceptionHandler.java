package com.BrainStack.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler global des exceptions pour l'API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ChildNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleChildNotFound(ChildNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Child not found", ex.getMessage());
    }

    @ExceptionHandler(ParentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleParentNotFound(ParentNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Parent not found", ex.getMessage());
    }

    @ExceptionHandler(HealthRecordNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleHealthRecordNotFound(HealthRecordNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Health record not found", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst().orElse("Validation error");
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
