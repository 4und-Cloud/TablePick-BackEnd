package com.goorm.tablepick.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getErrorCode().name(), e.getMessage()));
    }

    record ErrorResponse(String code, String message) {
    }
}