package ru.task.deduplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.task.deduplication.model.ProcessingQueueItem;

import java.time.LocalDateTime;
import java.util.List;

public interface ProcessingQueueRepository extends JpaRepository<ProcessingQueueItem, Long> {
    List<ProcessingQueueItem> findByProcessAtLessThanEqual(LocalDateTime dateTime);
}