package com.chargepoint.authenticationservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.response.topic}")
    private String responseTopic;

    public void sendAuthorizationResponse(String correlationId, String responseJson) {
        log.info("Sending authorization response to Kafka (correlationId={}): {}",
                correlationId, responseJson);
        kafkaTemplate.send(responseTopic, correlationId, responseJson);
    }
}
