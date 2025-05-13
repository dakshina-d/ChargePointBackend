package com.chargepoint.authenticationservice;

import com.chargepoint.authenticationservice.kafka.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        // initialize with mock template and override the @Value field
        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
        ReflectionTestUtils.setField(
                kafkaProducerService,
                "responseTopic",
                "auth-response-topic"
        );
    }

    @Test
    void shouldSendAuthorizationResponse() {
        // given
        String correlationId = "corr-abc";
        String responseJson = "{\"authorizationStatus\":\"Accepted\",\"correlationId\":\"corr-abc\"}";

        // when
        kafkaProducerService.sendAuthorizationResponse(correlationId, responseJson);

        // then
        // verify that the KafkaTemplate was called with topic, key, and payload
        verify(kafkaTemplate).send(
                "auth-response-topic",
                correlationId,
                responseJson
        );
    }

//    @Mock
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    private KafkaProducerService kafkaProducerService;
//
//    @BeforeEach
//    void setUp() {
//        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
//        ReflectionTestUtils.setField(kafkaProducerService, "responseTopic", "auth-response-topic");
//    }
//
//    @Test
//    void shouldSendAuthorizationResponse() {
//        String correlationId = "corr-abc";
//        String responseJson = "{\"status\":\"Accepted\"}";
//
//        kafkaProducerService.sendAuthorizationResponse(correlationId, responseJson);
//
//        verify(kafkaTemplate).send("auth-response-topic", correlationId, responseJson);
//    }
}
