package com.chargepoint.authenticationservice.service;

import com.chargepoint.authenticationservice.dto.AuthorizationRequest;
import com.chargepoint.authenticationservice.dto.AuthorizationResponse;
import com.chargepoint.authenticationservice.dto.DriverIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationProcessorServiceTest {

    private AuthorizationProcessorService service;

    @BeforeEach
    void setUp() {
        service = new AuthorizationProcessorService();
    }

    @Test
    void shouldReturnAcceptedForAllowedIdentifier() {
        AuthorizationRequest req = new AuthorizationRequest(
                "station1",
                new DriverIdentifier("12345678901234567890"),
                "cid"
        );
        AuthorizationResponse resp = service.process(req, "cid");
        assertEquals("Accepted", resp.getAuthorizationStatus());
        assertEquals("cid", resp.getCorrelationId());
    }

    @Test
    void shouldReturnRejectedForNotAllowedIdentifier() {
        AuthorizationRequest req = new AuthorizationRequest(
                "station2",
                new DriverIdentifier("12345678901234567891"),
                "cid"
        );
        AuthorizationResponse resp = service.process(req, "cid");
        assertEquals("Rejected", resp.getAuthorizationStatus());
        assertEquals("cid", resp.getCorrelationId());
    }

    @Test
    void shouldReturnUnknownForNonWhitelistedIdentifier() {
        AuthorizationRequest req = new AuthorizationRequest(
                "station3",
                new DriverIdentifier("99999999999999999999"),
                "cid"
        );
        AuthorizationResponse resp = service.process(req, "cid");
        assertEquals("Unknown", resp.getAuthorizationStatus());
        assertEquals("cid", resp.getCorrelationId());
    }

    @Test
    void shouldReturnInvalidForShortIdentifier() {
        AuthorizationRequest req = new AuthorizationRequest(
                "station4",
                new DriverIdentifier("short-id"),
                "cid"
        );
        AuthorizationResponse resp = service.process(req, "cid");
        assertEquals("Invalid", resp.getAuthorizationStatus());
        assertEquals("cid", resp.getCorrelationId());
    }

    @Test
    void shouldReturnInvalidForTooLongIdentifier() {
        String longId = "x".repeat(81);
        AuthorizationRequest req = new AuthorizationRequest(
                "station5",
                new DriverIdentifier(longId),
                "cid"
        );
        AuthorizationResponse resp = service.process(req, "cid");
        assertEquals("Invalid", resp.getAuthorizationStatus());
        assertEquals("cid", resp.getCorrelationId());
    }
}