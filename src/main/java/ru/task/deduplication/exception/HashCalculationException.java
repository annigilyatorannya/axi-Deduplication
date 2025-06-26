package ru.task.deduplication.exception;

public class HashCalculationException extends RuntimeException {
    public HashCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}