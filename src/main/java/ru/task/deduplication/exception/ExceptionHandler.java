package ru.task.deduplication.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.task.deduplication.dto.ErrorResponse;

import java.security.NoSuchAlgorithmException;

public class ExceptionHandler {
    public static ErrorResponse handleException(Exception e, String path) {
        if (e instanceof NoSuchAlgorithmException) {
            return new ErrorResponse(
                    "HASH_CALCULATION_ERROR",
                    "Error during hash calculation: " + e.getMessage()
            );
        } else if (e instanceof JsonProcessingException) {
            return new ErrorResponse(
                    "JSON_PROCESSING_ERROR",
                    "Error during JSON processing: " + e.getMessage()
            );
        } else {
            return new ErrorResponse(
                    "INTERNAL_SERVER_ERROR",
                    "Unexpected error: " + e.getMessage()
            );
        }
    }
}