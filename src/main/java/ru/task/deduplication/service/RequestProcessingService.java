package ru.task.deduplication.service;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.task.deduplication.model.ProcessingQueueItem;
import ru.task.deduplication.model.Request;
import ru.task.deduplication.model.StatusHistory;
import ru.task.deduplication.repository.ProcessingQueueRepository;
import ru.task.deduplication.repository.RequestRepository;
import ru.task.deduplication.repository.StatusHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.task.deduplication.model.StatusHistory.Status.*;

@Data
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestProcessingService {
    private final StatusHistoryRepository statusHistoryRepository;
    private final RequestRepository requestRepository;
    private final ProcessingQueueRepository queueRepository;

    @Transactional
    public void addInitialStatus(Request request, StatusHistory.Status status) {
        addStatus(request, status);

        if (status == RECEIVED) {
            ProcessingQueueItem queueItem = ProcessingQueueItem.builder()
                    .request(request)
                    .processAt(LocalDateTime.now())
                    .build();
            queueRepository.save(queueItem);
            log.info("Added request {} to processing queue", request.getId());
        }
    }
    @Scheduled(fixedRateString = "${app.scheduler.rate}")
    @Transactional
    public void processPendingRequests() {
        List<ProcessingQueueItem> queueItems = queueRepository
                .findByProcessAtLessThanEqual(LocalDateTime.now());
        if (queueItems.isEmpty()) {
            log.debug("No items in queue to process");
            return;
        }
        log.info("Found {} items to process", queueItems.size());
        queueItems.forEach(item -> {
            try {
                processRequest(item.getRequest());
                queueRepository.delete(item);
                log.info("Processed and removed request {} from queue", item.getRequest().getId());
            } catch (Exception e) {
                log.error("Error processing request {}: {}", item.getRequest().getId(), e.getMessage());
            }
        });
    }

    @Transactional
    protected void processRequest(Request request) {
        int updatedRows = statusHistoryRepository
                .updateStatusIfNotCompletedOrFailed(request.getId(), PROCESSING.name());

        if (updatedRows == 0) {
            return;
        }

        try {
            Thread.sleep(3500);
            addStatus(request, STAGE_1);

            Thread.sleep(3500);
            addStatus(request, STAGE_2);

            Thread.sleep(3500);
            addStatus(request, COMPLETED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            addStatus(request, FAILED);
        }
    }

    public StatusHistory.Status getCurrentStatus(Long requestId) {
        return statusHistoryRepository.findTopByRequestIdOrderByTimestampDesc(requestId)
                .map(StatusHistory::getStatus)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus
                        .NOT_FOUND, "Status not found for request id: " + requestId));
    }

    private void addStatus(Request request, StatusHistory.Status status) {
        StatusHistory history = StatusHistory.builder()
                .request(request)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);
    }
}