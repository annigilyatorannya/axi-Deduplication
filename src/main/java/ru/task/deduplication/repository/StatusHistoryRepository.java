package ru.task.deduplication.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.task.deduplication.model.StatusHistory;

import java.util.Optional;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    Optional<StatusHistory> findTopByRequestIdOrderByTimestampDesc(Long requestId);

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE status_history SET status = :newStatus " +
                    "WHERE request_id = :requestId AND status NOT IN ('COMPLETED', 'FAILED')",
            nativeQuery = true
    )
    int updateStatusIfNotCompletedOrFailed(
            @Param("requestId") Long requestId,
            @Param("newStatus") String newStatus
    );
}
