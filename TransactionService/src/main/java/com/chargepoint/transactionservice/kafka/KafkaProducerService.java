package com.chargepoint.transactionservice.kafka;

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

    @Value("${kafka.request.topic}")
    private String requestTopic;

    public void sendAuthorizationRequest(String requestJson) {
        log.info("Sending authorization request to Kafka: {}", requestJson);
        kafkaTemplate.send(requestTopic, requestJson);
    }
}
