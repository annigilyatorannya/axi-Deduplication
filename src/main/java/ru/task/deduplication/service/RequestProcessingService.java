package ru.task.deduplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.task.deduplication.model.Request;
import ru.task.deduplication.model.StatusHistory;
import ru.task.deduplication.repository.RequestRepository;
import ru.task.deduplication.repository.StatusHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestProcessingService {
    private final StatusHistoryRepository statusHistoryRepository;
    private final RequestRepository requestRepository;

    @Transactional
    public void addInitialStatus(Request request, String status) {
        addStatus(request, status);
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processPendingRequests() {
        List<Request> requests = requestRepository.findRequestsToProcess();
        requests.forEach(this::processRequest);
    }

    @Transactional
    protected void processRequest(Request request) {
        try {
            String currentStatus = getCurrentStatus(request);
            if (!"RECEIVED".equals(currentStatus)) {
                return;
            }

            addStatus(request, "PROCESSING");
            Thread.sleep(3500);

            addStatus(request, "STAGE_1");
            Thread.sleep(3500);

            addStatus(request, "STAGE_2");
            Thread.sleep(3500);

            addStatus(request, "COMPLETED");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            addStatus(request, "FAILED");
        }
    }

    private String getCurrentStatus(Request request) {
        return statusHistoryRepository
                .findTopByRequestOrderByTimestampDesc(request)
                .map(StatusHistory::getStatus)
                .orElse("UNKNOWN");
    }

    private void addStatus(Request request, String status) {
        StatusHistory history = StatusHistory.builder()
                .request(request)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);
    }
}