package com.chargepoint.authenticationservice.service;

import com.chargepoint.authenticationservice.dto.AuthorizationRequest;
import com.chargepoint.authenticationservice.dto.AuthorizationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthorizationProcessorService {

    private static final Map<String, Boolean> WHITELIST = new HashMap<>();

    static {
        WHITELIST.put("12345678901234567890", true);  // Allowed
        WHITELIST.put("12345678901234567891", false); // Not allowed
    }

    public AuthorizationResponse process(AuthorizationRequest request) {
        String identifier = request.getDriverIdentifier().getId();

        log.info("Processing authorization for identifier: {}", identifier);

        if (identifier == null || identifier.length() < 20 || identifier.length() > 80) {
            return new AuthorizationResponse("Invalid");
        }

        Boolean allowed = WHITELIST.get(identifier);

        if (allowed == null) {
            return new AuthorizationResponse("Unknown");
        } else if (Boolean.TRUE.equals(allowed)) {
            return new AuthorizationResponse("Accepted");
        } else {
            return new AuthorizationResponse("Rejected");
        }
    }
}