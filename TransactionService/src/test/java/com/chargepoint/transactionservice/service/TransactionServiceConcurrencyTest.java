package com.chargepoint.transactionservice.service;

import com.chargepoint.transactionservice.dto.AuthorizationRequest;
import com.chargepoint.transactionservice.dto.AuthorizationResponse;
import com.chargepoint.transactionservice.dto.DriverIdentifier;
import com.chargepoint.transactionservice.kafka.KafkaConsumerService;
import com.chargepoint.transactionservice.kafka.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TransactionServiceConcurrencyTest {

    private KafkaProducerService producerService;
    private KafkaConsumerService consumerService;
    private TransactionService transactionService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        producerService = mock(KafkaProducerService.class);
        consumerService = mock(KafkaConsumerService.class);
        objectMapper = new ObjectMapper();
        transactionService = new TransactionService(producerService, consumerService, objectMapper);
    }

    @Test
    void shouldHandleMultipleConcurrentAuthorizationsIndependently() throws Exception {
        // Map to hold the stubbed futures keyed by correlationId
        ConcurrentMap<String, CompletableFuture<String>> stubFutures = new ConcurrentHashMap<>();

        // Stub registerCallback to return a new future for each correlationId
        when(consumerService.registerCallback(anyString()))
                .thenAnswer(invocation -> {
                    String cid = invocation.getArgument(0);
                    CompletableFuture<String> future = new CompletableFuture<>();
                    stubFutures.put(cid, future);
                    return future;
                });

        // Ignore actual send logic
        doNothing().when(producerService).sendAuthorizationRequest(anyString(), anyString());

        // Prepare two distinct requests
        AuthorizationRequest req1 = new AuthorizationRequest(
                "station1", new DriverIdentifier("AAAAAAAAAAAAAAAAAAAA"), null);
        AuthorizationRequest req2 = new AuthorizationRequest(
                "station2", new DriverIdentifier("BBBBBBBBBBBBBBBBBBBB"), null);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<AuthorizationResponse> f1 = executor.submit(() -> transactionService.authorize(req1));
        Future<AuthorizationResponse> f2 = executor.submit(() -> transactionService.authorize(req2));

        // Wait until both correlationIds have been registered
        while (stubFutures.size() < 2) {
            Thread.sleep(10);
        }

        // Complete each future with a matching JSON response
        for (Map.Entry<String, CompletableFuture<String>> entry : stubFutures.entrySet()) {
            String cid = entry.getKey();
            String jsonResponse = objectMapper.writeValueAsString(
                    new AuthorizationResponse("Accepted", cid));
            entry.getValue().complete(jsonResponse);
        }

        // Collect results
        AuthorizationResponse resp1 = f1.get(1, TimeUnit.SECONDS);
        AuthorizationResponse resp2 = f2.get(1, TimeUnit.SECONDS);

        // Each should be Accepted and have its own correlationId
        assertEquals("Accepted", resp1.getAuthorizationStatus());
        assertEquals("Accepted", resp2.getAuthorizationStatus());
        assertNotEquals(resp1.getCorrelationId(), resp2.getCorrelationId());

        // Verify producer was called twice, once per request
        verify(producerService, times(2))
                .sendAuthorizationRequest(anyString(), anyString());

        executor.shutdown();
    }
}