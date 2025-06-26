package ru.task.deduplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.task.deduplication.model.Request;

import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("SELECT r FROM Request r WHERE r.requestHash = :hash AND r.id IN " +
            "(SELECT sh.request.id FROM StatusHistory sh WHERE sh.status NOT IN ('COMPLETED', 'FAILED'))")
    Optional<Request> findActiveByHash(@Param("hash") String hash);
}