package com.chargepoint.transactionservice.service;

import com.chargepoint.transactionservice.dto.AuthorizationRequest;
import com.chargepoint.transactionservice.dto.AuthorizationResponse;
import com.chargepoint.transactionservice.dto.AuthorizationResponseDto;
import com.chargepoint.transactionservice.kafka.KafkaConsumerService;
import com.chargepoint.transactionservice.kafka.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final KafkaProducerService kafkaProducerService;
    private final KafkaConsumerService kafkaConsumerService;
    private final ObjectMapper objectMapper;

    public AuthorizationResponse authorize(AuthorizationRequest request) throws Exception {
        log.info("Processing authorization request for Station: {}, Driver ID: {}",
                request.getStationUuid(), request.getDriverIdentifier().getId());
        String correlationId = UUID.randomUUID().toString();

        request.setCorrelationId(correlationId);

        String jsonRequest = objectMapper.writeValueAsString(request);

        CompletableFuture<String> responseFuture =
                kafkaConsumerService.registerCallback(correlationId);

        kafkaProducerService.sendAuthorizationRequest(correlationId, jsonRequest);

        try {
            String jsonResponse = responseFuture.get(5, TimeUnit.SECONDS);
            log.info("Received Kafka response for correlationId={}: {}", correlationId, jsonResponse);

            return objectMapper.readValue(jsonResponse, AuthorizationResponse.class);
        } catch (Exception e) {
            log.error("Error during authorization process", e);
            throw new RuntimeException("Request Timeout or Failed to authorize");
        }
    }
}