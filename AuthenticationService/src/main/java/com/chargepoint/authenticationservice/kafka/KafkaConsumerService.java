package com.chargepoint.authenticationservice.kafka;

import com.chargepoint.authenticationservice.dto.AuthorizationRequest;
import com.chargepoint.authenticationservice.dto.AuthorizationResponse;
import com.chargepoint.authenticationservice.service.AuthorizationProcessorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final AuthorizationProcessorService authorizationProcessorService;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.request.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String message) throws Exception {
        log.info("Received authorization request: {}", message);
        JsonNode node = objectMapper.readTree(message);
        String correlationId = node.get("correlationId").asText();
        AuthorizationRequest request =
                objectMapper.treeToValue(node, AuthorizationRequest.class);

        AuthorizationResponse response = authorizationProcessorService.process(request, correlationId);
        response.setCorrelationId(correlationId);
        String responseJson = objectMapper.writeValueAsString(response);
        kafkaProducerService.sendAuthorizationResponse(correlationId, responseJson);
    }
}