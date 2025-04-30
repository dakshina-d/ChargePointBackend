package com.chargepoint.authenticationservice;

import com.chargepoint.authenticationservice.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Test Error");

        ResponseEntity<?> response = handler.handleExceptions(ex);

        assertEquals(500, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("timestamp"));
        assertEquals("Test Error", body.get("message"));
    }
}
