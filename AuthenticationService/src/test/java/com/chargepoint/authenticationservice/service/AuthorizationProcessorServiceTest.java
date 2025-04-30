package com.chargepoint.authenticationservice.service;

import com.chargepoint.authenticationservice.dto.AuthorizationRequest;
import com.chargepoint.authenticationservice.dto.AuthorizationResponse;
import com.chargepoint.authenticationservice.dto.DriverIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorizationProcessorServiceTest {

    private AuthorizationProcessorService authorizationProcessorService;

    @BeforeEach
    void setUp() {
        authorizationProcessorService = new AuthorizationProcessorService();
    }

    @Test
    void shouldReturnAcceptedForAllowedIdentifier() {
        AuthorizationRequest request = new AuthorizationRequest("station1",
                new DriverIdentifier("12345678901234567890"));
        AuthorizationResponse response = authorizationProcessorService.process(request);

        assertEquals("Accepted", response.getAuthorizationStatus());
    }

    @Test
    void shouldReturnRejectedForNotAllowedIdentifier() {
        AuthorizationRequest request = new AuthorizationRequest("station2",
                new DriverIdentifier("12345678901234567891"));
        AuthorizationResponse response = authorizationProcessorService.process(request);

        assertEquals("Rejected", response.getAuthorizationStatus());
    }

    @Test
    void shouldReturnUnknownForNonWhitelistedIdentifier() {
        AuthorizationRequest request = new AuthorizationRequest("station3",
                new DriverIdentifier("99999999999999999999"));
        AuthorizationResponse response = authorizationProcessorService.process(request);

        assertEquals("Unknown", response.getAuthorizationStatus());
    }

    @Test
    void shouldReturnInvalidForShortIdentifier() {
        AuthorizationRequest request = new AuthorizationRequest("station4",
                new DriverIdentifier("short-id"));
        AuthorizationResponse response = authorizationProcessorService.process(request);

        assertEquals("Invalid", response.getAuthorizationStatus());
    }
}
