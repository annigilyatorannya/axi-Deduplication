package ru.task.deduplication.dto;

import lombok.Data;
import ru.task.deduplication.model.StatusHistory;
@Data
public class StatusResponseDto {
    private StatusHistory.Status currentStatus;

    public StatusResponseDto(StatusHistory.Status currentStatus) {
        this.currentStatus = currentStatus;
    }
}
