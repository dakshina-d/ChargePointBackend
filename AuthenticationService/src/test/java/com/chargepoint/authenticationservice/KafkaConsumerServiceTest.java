package com.chargepoint.authenticationservice;

import com.chargepoint.authenticationservice.dto.AuthorizationRequest;
import com.chargepoint.authenticationservice.dto.AuthorizationResponse;
import com.chargepoint.authenticationservice.dto.DriverIdentifier;
import com.chargepoint.authenticationservice.kafka.KafkaConsumerService;
import com.chargepoint.authenticationservice.kafka.KafkaProducerService;
import com.chargepoint.authenticationservice.service.AuthorizationProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {


    @Mock
    private AuthorizationProcessorService processorService;

    @Mock
    private KafkaProducerService producerService;

    private KafkaConsumerService consumerService;
    private ObjectMapper mapper;

    @Captor
    private ArgumentCaptor<String> correlationIdCaptor;

    @Captor
    private ArgumentCaptor<String> responseJsonCaptor;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        consumerService = new KafkaConsumerService(processorService, producerService, mapper);
    }

    @Test
    void shouldConsumeMessageAndSendResponse() throws Exception {
        // given
        String correlationId = "corr-123";
        AuthorizationRequest incoming = new AuthorizationRequest(
                "stationX",
                new DriverIdentifier("12345678901234567890"),
                correlationId
        );

        // stub the two-arg process(request, correlationId)
        AuthorizationResponse processorOutput = new AuthorizationResponse("Accepted", null);
        when(processorService.process(any(AuthorizationRequest.class), eq(correlationId)))
                .thenReturn(processorOutput);

        // serialize incoming JSON exactly as Kafka would deliver it
        String jsonInput = mapper.writeValueAsString(incoming);

        // when
        consumerService.listen(jsonInput);

        // then
        // 1) process(request, correlationId) was called
        verify(processorService).process(any(AuthorizationRequest.class), eq(correlationId));

        // 2) sendAuthorizationResponse(correlationId, responseJson) was called
        verify(producerService).sendAuthorizationResponse(
                correlationIdCaptor.capture(),
                responseJsonCaptor.capture()
        );

        // assert the captured correlationId matches
        assertEquals(correlationId, correlationIdCaptor.getValue());

        // assert the JSON sent to Kafka has both status and the same correlationId
        AuthorizationResponse sent = mapper.readValue(
                responseJsonCaptor.getValue(),
                AuthorizationResponse.class
        );
        assertEquals("Accepted", sent.getAuthorizationStatus());
        assertEquals(correlationId, sent.getCorrelationId());
    }

//    @Mock
//    private AuthorizationProcessorService processorService;
//
//    @Mock
//    private KafkaProducerService producerService;
//
//    private KafkaConsumerService consumerService;
//    private ObjectMapper mapper;
//
//    @BeforeEach
//    void setup() {
//        mapper = new ObjectMapper();
//        // match your actual constructor: (processorService, producerService, objectMapper)
//        consumerService = new KafkaConsumerService(processorService, producerService, mapper);
//    }
//
//    @Test
//    void shouldConsumeMessageAndSendResponse() throws Exception {
//        // given
//        String correlationId = "corr-123";
//        AuthorizationRequest mockRequest =
//                new AuthorizationRequest(
//                        "stationX",
//                        new DriverIdentifier("12345678901234567890"),
//                        correlationId
//                );
//
//        // processor returns a response WITHOUT correlationId (the listener will add it)
//        AuthorizationResponse mockResponse = new AuthorizationResponse("Accepted", null);
//        when(processorService.process(any(AuthorizationRequest.class), correlationId))
//                .thenReturn(mockResponse);
//
//        String jsonInput = mapper.writeValueAsString(mockRequest);
//
//        // expected JSON now has both status and correlationId
//        AuthorizationResponse expectedResponse =
//                new AuthorizationResponse("Accepted", correlationId);
//        String jsonOutput = mapper.writeValueAsString(expectedResponse);
//
//        // when
//        consumerService.listen(jsonInput);
//
//        // then
//        verify(processorService).process(any(AuthorizationRequest.class), correlationId);
//        verify(producerService).sendAuthorizationResponse(
//                eq(correlationId),
//                eq(jsonOutput)
//        );
//    }
}

