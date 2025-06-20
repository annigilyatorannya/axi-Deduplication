package ru.task.deduplication.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.task.deduplication.model.Request;
import ru.task.deduplication.repository.RequestRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeduplicationService {
    private final RequestRepository requestRepository;

    @Transactional
    public Optional<Long> findDuplicateRequest(String jsonPayload) {
        String hash = calculateRequestHash(jsonPayload);
        Optional<Request> duplicate = requestRepository.findActiveByHash(hash);

        if (duplicate.isPresent()) {
            Request request = duplicate.get();
            request.setDuplicateCount(request.getDuplicateCount() + 1);
            requestRepository.save(request);
            return Optional.of(request.getId());
        }

        return Optional.empty();
    }

    public String calculateRequestHash(String json) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
}