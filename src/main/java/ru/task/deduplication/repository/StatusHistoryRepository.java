package ru.task.deduplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.task.deduplication.model.Request;
import ru.task.deduplication.model.StatusHistory;

import java.util.List;
import java.util.Optional;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByRequestOrderByTimestampDesc(Request request);
    Optional<StatusHistory> findTopByRequestOrderByTimestampDesc(Request request);
}
