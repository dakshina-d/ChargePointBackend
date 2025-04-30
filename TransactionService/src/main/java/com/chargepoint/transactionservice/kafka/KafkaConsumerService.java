package com.chargepoint.transactionservice.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class KafkaConsumerService {

    @Getter
    private CompletableFuture<String> future = new CompletableFuture<>();

    @KafkaListener(topics = "${kafka.response.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) {
        log.info("Received authorization response from Kafka: {}", message);
        future.complete(message);
        future = new CompletableFuture<>();
    }
}
