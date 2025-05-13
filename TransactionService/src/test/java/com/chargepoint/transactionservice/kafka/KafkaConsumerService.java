package com.chargepoint.transactionservice.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"auth-response-topic"})
class KafkaConsumerServiceTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private String testCorrelationId = "test-correlation-id";

    @Test
    void shouldCompleteFutureWithCorrectMessage() throws Exception {
        String testMessage = "{\"correlationId\":\"" + testCorrelationId + "\",\"authorizationStatus\":\"Accepted\"}";

        CompletableFuture<String> future = kafkaConsumerService.registerCallback(testCorrelationId);

        kafkaTemplate.send(new ProducerRecord<>("auth-response-topic", testMessage));

        String result = future.get(10, TimeUnit.SECONDS);

        assertThat(result).isEqualTo(testMessage);
    }
}