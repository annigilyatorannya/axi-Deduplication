package ru.task.deduplication.dto;

import lombok.*;

@Data
@NoArgsConstructor
public class RequestResponseDto {
    private boolean duplicate;
    private Long requestId;

    public RequestResponseDto(boolean duplicate, Long requestId) {
        this.duplicate = duplicate;
        this.requestId = requestId;
    }
    public static RequestResponseDto duplicateResponse(Long requestId) {
        return new RequestResponseDto(true, requestId);
    }

    public static RequestResponseDto newRequestResponse(Long requestId) {
        return new RequestResponseDto(false, requestId);
    }

}