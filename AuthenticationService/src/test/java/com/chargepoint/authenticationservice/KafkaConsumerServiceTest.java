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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @BeforeEach
    void setup() {
        consumerService = new KafkaConsumerService(processorService, producerService);
    }

    @Test
    void shouldConsumeMessageAndSendResponse() throws Exception {
        AuthorizationRequest mockRequest = new AuthorizationRequest("stationX", new DriverIdentifier("12345678901234567890"));
        AuthorizationResponse mockResponse = new AuthorizationResponse("Accepted");

        when(processorService.process(any())).thenReturn(mockResponse);

        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(mockRequest);
        String jsonOutput = mapper.writeValueAsString(mockResponse);

        consumerService.listen(jsonInput);

        verify(processorService).process(any());
        verify(producerService).sendAuthorizationResponse(eq(jsonOutput));
    }
}

