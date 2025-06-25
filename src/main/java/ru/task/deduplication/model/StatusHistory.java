package ru.task.deduplication.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    @JsonBackReference
    private Request request;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime timestamp;

    public enum Status {
        RECEIVED,
        PROCESSING,
        STAGE_1,
        STAGE_2,
        COMPLETED,
        FAILED
    }
}