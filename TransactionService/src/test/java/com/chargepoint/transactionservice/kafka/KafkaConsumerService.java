package com.chargepoint.transactionservice.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "auth-response-topic" })
class KafkaConsumerServiceTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    void setUp() {
        // No additional setup needed, the KafkaConsumerService will consume messages via @KafkaListener
    }

    @Test
    void shouldConsumeAuthorizationResponse() throws Exception {
        // Arrange
        String testMessage = "{\"authorizationStatus\":\"Accepted\"}";

        // Act
        kafkaTemplate.send(new ProducerRecord<>("auth-response-topic", testMessage));

        // Assert
        String receivedMessage = kafkaConsumerService.getFuture().get(10, TimeUnit.SECONDS); // wait up to 10 seconds
        assertThat(receivedMessage).isEqualTo(testMessage);
    }
}