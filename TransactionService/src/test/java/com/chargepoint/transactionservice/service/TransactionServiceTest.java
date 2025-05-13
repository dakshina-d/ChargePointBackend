package com.chargepoint.transactionservice.service;

import com.chargepoint.transactionservice.dto.AuthorizationRequest;
import com.chargepoint.transactionservice.dto.AuthorizationResponse;
import com.chargepoint.transactionservice.dto.AuthorizationResponseDto;
import com.chargepoint.transactionservice.dto.DriverIdentifier;
import com.chargepoint.transactionservice.kafka.KafkaConsumerService;
import com.chargepoint.transactionservice.kafka.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private KafkaProducerService kafkaProducerService;
    private KafkaConsumerService kafkaConsumerService;
    private TransactionService transactionService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        kafkaProducerService = mock(KafkaProducerService.class);
        kafkaConsumerService = mock(KafkaConsumerService.class);
        transactionService = new TransactionService(kafkaProducerService, kafkaConsumerService, objectMapper);
    }

    @Test
    void shouldReturnAuthorizationResponse() throws Exception {

        AuthorizationRequest request = new AuthorizationRequest("stationUuid", new DriverIdentifier("12345678901234567890"), "");
        AuthorizationResponse response = new AuthorizationResponse("Accepted", "");
        String jsonResponse = objectMapper.writeValueAsString(response);
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete(jsonResponse);

        when(kafkaConsumerService.registerCallback(anyString())).thenReturn(future);

        AuthorizationResponse result = transactionService.authorize(request);

        assertEquals("Accepted", result.getAuthorizationStatus());
        verify(kafkaProducerService, times(1)).sendAuthorizationRequest(anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenFutureFails() throws Exception {
        AuthorizationRequest request = new AuthorizationRequest("stationUuid", new DriverIdentifier("12345678901234567890"), "");
        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka failure"));

        when(kafkaConsumerService.registerCallback(anyString())).thenReturn(failedFuture);

        assertThrows(Exception.class, () -> transactionService.authorize(request));
    }
}