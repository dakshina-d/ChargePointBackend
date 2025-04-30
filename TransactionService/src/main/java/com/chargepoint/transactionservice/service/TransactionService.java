package com.chargepoint.transactionservice.service;

import com.chargepoint.transactionservice.dto.AuthorizationRequest;
import com.chargepoint.transactionservice.dto.AuthorizationResponse;
import com.chargepoint.transactionservice.kafka.KafkaConsumerService;
import com.chargepoint.transactionservice.kafka.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final KafkaProducerService kafkaProducerService;
    private final KafkaConsumerService kafkaConsumerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthorizationResponse authorize(AuthorizationRequest request) throws Exception {
        log.info("Processing authorization request for Station: {}, Driver ID: {}",
                request.getStationUuid(), request.getDriverIdentifier().getId());

        String jsonRequest = objectMapper.writeValueAsString(request);
        kafkaProducerService.sendAuthorizationRequest(jsonRequest);

        try {
            String jsonResponse = kafkaConsumerService.getFuture().get(5, TimeUnit.SECONDS);
            log.info("Authorization response received: {}", jsonResponse);
            return objectMapper.readValue(jsonResponse, AuthorizationResponse.class);
        } catch (Exception e) {
            log.error("Error during authorization process", e);
            throw new RuntimeException("Request Timeout or Failed to authorize");
        }
    }
}