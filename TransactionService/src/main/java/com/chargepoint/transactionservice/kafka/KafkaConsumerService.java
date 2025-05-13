package com.chargepoint.transactionservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;
    private final Map<String, CompletableFuture<String>> futureMap = new ConcurrentHashMap<>();

    public KafkaConsumerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<String> registerCallback(String correlationId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        futureMap.put(correlationId, future);
        return future;
    }

    @KafkaListener(
            topics = "${kafka.response.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(String message) {
        log.info("Received authorization response from Kafka: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            String correlationId = node.get("correlationId").asText();
            CompletableFuture<String> future = futureMap.remove(correlationId);
            if (future != null) {
                future.complete(message);
            } else {
                log.warn("No pending request for correlationId: {}", correlationId);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Kafka response", e);
        }
    }
}
