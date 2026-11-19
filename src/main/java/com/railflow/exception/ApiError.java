package com.railflow.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> errors
) {
    public ApiError(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message, null);
    }

    public ApiError(int status, String error, String message, Map<String, String> errors) {
        this(LocalDateTime.now(), status, error, message, errors);
    }
}
