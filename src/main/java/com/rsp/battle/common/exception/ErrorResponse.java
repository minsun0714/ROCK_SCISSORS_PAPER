package com.rsp.battle.common.exception;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

public record ErrorResponse(
        String errorCode,
        String message,
        String method,
        String requestURI,
        String timestamp
) {

    public static ErrorResponse of(ErrorCode errorCode, HttpServletRequest request) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getMethod(),
                request.getRequestURI(),
                LocalDateTime.now().toString()
        );
    }
}
