package ru.task.deduplication.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.task.deduplication.dto.RequestResponseDto;
import ru.task.deduplication.dto.StatusResponseDto;
import ru.task.deduplication.model.Request;
import ru.task.deduplication.model.StatusHistory;
import ru.task.deduplication.repository.RequestRepository;
import ru.task.deduplication.service.DeduplicationService;
import ru.task.deduplication.service.RequestProcessingService;

import java.util.Optional;

import static ru.task.deduplication.model.StatusHistory.Status.RECEIVED;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {
    private final DeduplicationService deduplicationService;
    private final RequestProcessingService processingService;
    private final RequestRepository requestRepository;

    @PostMapping
    public ResponseEntity<RequestResponseDto> submitRequest(@RequestBody String jsonPayload) {
        Optional<Long> duplicateId = deduplicationService.findDuplicateRequest(jsonPayload);
        if (duplicateId.isPresent()) {
            return ResponseEntity.ok(RequestResponseDto.duplicateResponse(duplicateId.get()));
        }

        Request request = new Request();
        request.setJsonPayload(jsonPayload);
        request.setRequestHash(deduplicationService.calculateRequestHash(jsonPayload));
        request = requestRepository.save(request);

        processingService.addInitialStatus(request, RECEIVED);

        return ResponseEntity.ok(RequestResponseDto.newRequestResponse(request.getId()));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<StatusResponseDto> getRequestStatus(@PathVariable Long id) {
        StatusHistory.Status currentStatus = processingService.getCurrentStatus(id);
        return ResponseEntity.ok(new StatusResponseDto(currentStatus));
    }
}