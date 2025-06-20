package ru.task.deduplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.task.deduplication.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("SELECT r FROM Request r WHERE r.requestHash = :hash AND r.id IN " +
            "(SELECT sh.request.id FROM StatusHistory sh WHERE sh.status NOT IN ('COMPLETED', 'FAILED'))")
    Optional<Request> findActiveByHash(@Param("hash") String hash);
    @Query("SELECT r FROM Request r WHERE r.id IN " +
            "(SELECT sh.request.id FROM StatusHistory sh " +
            "WHERE sh.status = 'RECEIVED' AND sh.timestamp = " +
            "(SELECT MAX(sh2.timestamp) FROM StatusHistory sh2 WHERE sh2.request = sh.request) " +
            "AND NOT EXISTS (SELECT 1 FROM StatusHistory sh3 WHERE sh3.request = sh.request AND sh3.status = 'PROCESSING'))")
    List<Request> findRequestsToProcess();
}