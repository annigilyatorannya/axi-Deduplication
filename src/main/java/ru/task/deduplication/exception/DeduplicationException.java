package ru.task.deduplication.exception;

import ru.task.deduplication.dto.ErrorResponse;

public class DeduplicationException extends RuntimeException {
    private final ErrorResponse errorResponse;

    public DeduplicationException(ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}