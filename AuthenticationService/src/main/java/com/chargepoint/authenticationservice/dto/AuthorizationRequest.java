package com.chargepoint.authenticationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationRequest {
    private String stationUuid;
    private DriverIdentifier driverIdentifier;
}
