package ru.task.deduplication.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    String code;
    String message;
//    String path;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}