package com.chargepoint.transactionservice.controller;

import com.chargepoint.transactionservice.dto.AuthorizationRequest;
import com.chargepoint.transactionservice.dto.AuthorizationResponse;
import com.chargepoint.transactionservice.dto.AuthorizationResponseDto;
import com.chargepoint.transactionservice.dto.DriverIdentifier;
import com.chargepoint.transactionservice.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return 200 OK when authorization is successful")
    void testAuthorizeSuccess() throws Exception {

        AuthorizationRequest request = new AuthorizationRequest("stationUuid", new DriverIdentifier("12345678901234567890"), "");
        AuthorizationResponse response = new AuthorizationResponse("Accepted", "");

        when(transactionService.authorize(any(AuthorizationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/transaction/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorizationStatus").value("Accepted"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when driver ID is invalid")
    void testAuthorizeValidationFailure() throws Exception {
        AuthorizationRequest request = new AuthorizationRequest("stationUuid", new DriverIdentifier("short"), "");

        mockMvc.perform(post("/transaction/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when service fails")
    void testAuthorizeServiceException() throws Exception {
        AuthorizationRequest request = new AuthorizationRequest("stationUuid", new DriverIdentifier("12345678901234567890"), "");

        when(transactionService.authorize(any(AuthorizationRequest.class))).thenThrow(new RuntimeException("Kafka error"));

        mockMvc.perform(post("/transaction/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Kafka error"));
    }
}