package com.chargepoint.transactionservice.service;

import com.chargepoint.transactionservice.dto.AuthorizationRequest;
import com.chargepoint.transactionservice.dto.AuthorizationResponse;
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
        transactionService = new TransactionService(kafkaProducerService, kafkaConsumerService);
    }

    @Test
    void shouldReturnAuthorizationResponse() throws Exception {
        AuthorizationRequest request = new AuthorizationRequest("stationUuid",
                new DriverIdentifier("12345678901234567890"));

        AuthorizationResponse expectedResponse = new AuthorizationResponse("Accepted");
        String jsonResponse = objectMapper.writeValueAsString(expectedResponse);

        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete(jsonResponse);

        when(kafkaConsumerService.getFuture()).thenReturn(future);

        AuthorizationResponse actualResponse = transactionService.authorize(request);

        assertEquals(expectedResponse.getAuthorizationStatus(), actualResponse.getAuthorizationStatus());
        verify(kafkaProducerService, times(1)).sendAuthorizationRequest(anyString());
    }

    @Test
    void shouldThrowExceptionWhenFutureFails() throws Exception {
        AuthorizationRequest request = new AuthorizationRequest("stationUuid",
                new DriverIdentifier("12345678901234567890"));

        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka failure"));

        when(kafkaConsumerService.getFuture()).thenReturn(failedFuture);

        assertThrows(Exception.class, () -> transactionService.authorize(request));

        verify(kafkaProducerService, times(1)).sendAuthorizationRequest(anyString());
    }

}
