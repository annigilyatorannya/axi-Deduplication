package ru.task.deduplication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.task.deduplication.dto.ErrorResponse;
import ru.task.deduplication.exception.DeduplicationException;
import ru.task.deduplication.exception.ExceptionHandler;
import ru.task.deduplication.model.Request;
import ru.task.deduplication.repository.RequestRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeduplicationService {
    private final RequestRepository requestRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Optional<Long> findDuplicateRequest(String jsonPayload) {
        String hash = calculateRequestHash(jsonPayload);
        Optional<Request> activeRequest = requestRepository.findActiveByHash(hash);
        return activeRequest.map(Request::getId);
    }

    public String calculateRequestHash(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.isObject()) {
                node = sortObjectNode((ObjectNode) node);
            }
            String normalizedJson = objectMapper.writeValueAsString(node);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizedJson.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            ErrorResponse error = ExceptionHandler.handleException(e, "/calculate-hash");
            throw new DeduplicationException(error);
        }
    }

    private JsonNode sortObjectNode(ObjectNode node) {
        ObjectNode sortedNode = objectMapper.createObjectNode();
        List<String> fieldNames = new ArrayList<>();
        node.fieldNames().forEachRemaining(fieldNames::add);
        Collections.sort(fieldNames);

        for (String fieldName : fieldNames) {
            JsonNode fieldValue = node.get(fieldName);
            if (fieldValue.isObject()) {
                fieldValue = sortObjectNode((ObjectNode) fieldValue);
            }
            sortedNode.set(fieldName, fieldValue);
        }
        return sortedNode;
    }
}