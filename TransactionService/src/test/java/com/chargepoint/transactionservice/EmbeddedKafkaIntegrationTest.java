package com.chargepoint.transactionservice;

import com.chargepoint.transactionservice.dto.AuthorizationRequest;
import com.chargepoint.transactionservice.dto.AuthorizationResponse;
import com.chargepoint.transactionservice.dto.DriverIdentifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"auth-request-topic", "auth-response-topic"})
@TestPropertySource(properties = {
        "kafka.request.topic=auth-request-topic",
        "kafka.response.topic=auth-response-topic",
        "spring.kafka.consumer.group-id=test-group"
})
@Import(EmbeddedKafkaIntegrationTest.TestKafkaConfig.class)
public class EmbeddedKafkaIntegrationTest {

    private static final String REQUEST_TOPIC = "auth-request-topic";
    private static final String RESPONSE_TOPIC = "auth-response-topic";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private KafkaTemplate<String, String> kafkaTemplate;

    @TestConfiguration
    static class TestKafkaConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Before
    public void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        DefaultKafkaProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer());
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @After
    public void tearDown() {
        if (kafkaTemplate != null) {
            kafkaTemplate.destroy();
        }
    }

    @KafkaListener(topics = REQUEST_TOPIC, groupId = "test-group")
    public void listenRequest(String message) throws Exception {
        AuthorizationResponse response = new AuthorizationResponse();
        response.setAuthorizationStatus("Accepted");

        kafkaTemplate.send(RESPONSE_TOPIC, objectMapper.writeValueAsString(response));
    }

    @Test
    public void testAuthorizeEndpoint() throws Exception {
        AuthorizationRequest request = new AuthorizationRequest();
        DriverIdentifier driver = new DriverIdentifier();
        driver.setId("12345678901234567891"); // Valid length
        request.setStationUuid("25aac66b-6051-478a-95e2-6d3aa343b025");
        request.setDriverIdentifier(driver);

        String jsonRequest = objectMapper.writeValueAsString(request);

        String responseContent = mockMvc.perform(post("/transaction/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthorizationResponse response = objectMapper.readValue(responseContent, AuthorizationResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getAuthorizationStatus()).isEqualTo("Accepted");
    }
}
