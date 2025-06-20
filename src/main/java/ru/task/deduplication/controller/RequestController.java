package ru.task.deduplication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.task.deduplication.model.Request;
import ru.task.deduplication.model.StatusHistory;
import ru.task.deduplication.repository.RequestRepository;
import ru.task.deduplication.repository.StatusHistoryRepository;
import ru.task.deduplication.service.DeduplicationService;
import ru.task.deduplication.service.RequestProcessingService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {
    private final DeduplicationService deduplicationService;
    private final RequestProcessingService processingService;
    private final RequestRepository requestRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitRequest(@RequestBody String jsonPayload) {
        Optional<Long> duplicateId = deduplicationService.findDuplicateRequest(jsonPayload);
        if (duplicateId.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "duplicate", true,
                    "requestId", duplicateId.get()
            ));
        }

        Request request = new Request();
        request.setJsonPayload(jsonPayload);
        request.setRequestHash(deduplicationService.calculateRequestHash(jsonPayload));
        request = requestRepository.save(request);

        processingService.addInitialStatus(request, "RECEIVED");

        return ResponseEntity.ok(Map.of(
                "duplicate", false,
                "requestId", request.getId()
        ));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> getRequestStatus(@PathVariable Long id) {
        Optional<Request> requestOpt = requestRepository.findById(id);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<StatusHistory> history = statusHistoryRepository.findByRequestOrderByTimestampDesc(requestOpt.get());
        String currentStatus = history.isEmpty() ? "UNKNOWN" : history.get(0).getStatus();

        return ResponseEntity.ok(Map.of(
                "currentStatus", currentStatus,
                "history", history.stream()
                        .map(h -> Map.of(
                                "status", h.getStatus(),
                                "timestamp", h.getTimestamp()
                        ))
                        .collect(Collectors.toList())
        ));
    }
}