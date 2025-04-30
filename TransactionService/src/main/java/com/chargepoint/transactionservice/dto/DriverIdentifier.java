package com.chargepoint.transactionservice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverIdentifier {

    @Size(min = 20, max = 80, message = "Identifier must be between 20 and 80 characters")
    private String id;
}