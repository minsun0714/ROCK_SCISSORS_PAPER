package com.rsp.battle.common.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("[BusinessException] code={}, message={}, path={}",
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        log.warn("[ValidationException] path={}, error={}",
                request.getRequestURI(),
                e.getMessage()
        );

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("[UnhandledException] path={}", request.getRequestURI(), e);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, request));
    }
}