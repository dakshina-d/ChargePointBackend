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
        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
        ReflectionTestUtils.setField(kafkaProducerService, "responseTopic", "auth-response-topic");
    }

    @Test
    void shouldSendAuthorizationResponse() {
        kafkaProducerService.sendAuthorizationResponse("{\"status\":\"Accepted\"}");

        verify(kafkaTemplate).send("auth-response-topic", "{\"status\":\"Accepted\"}");
    }
}
