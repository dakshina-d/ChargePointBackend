package com.chargepoint.transactionservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationRequest {

    @NotBlank
    private String stationUuid;

    @Valid
    private DriverIdentifier driverIdentifier;
}
