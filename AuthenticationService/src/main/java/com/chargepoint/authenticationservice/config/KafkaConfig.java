package com.chargepoint.authenticationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic authRequestTopic() {
        return new NewTopic("auth-request-topic", 1, (short) 1);
    }

    @Bean
    public NewTopic authResponseTopic() {
        return new NewTopic("auth-response-topic", 1, (short) 1);
    }
}
