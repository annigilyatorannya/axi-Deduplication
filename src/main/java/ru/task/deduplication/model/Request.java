package ru.task.deduplication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String jsonPayload;

    private String requestHash;
    private int duplicateCount;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    @OrderBy("timestamp DESC")
    @JsonIgnore
    private List<StatusHistory> statusHistory = new ArrayList<>();
}