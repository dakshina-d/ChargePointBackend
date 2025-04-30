package com.chargepoint.transactionservice.controller;

import com.chargepoint.transactionservice.dto.AuthorizationRequest;
import com.chargepoint.transactionservice.dto.AuthorizationResponse;
import com.chargepoint.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizationResponse> authorize(@Valid @RequestBody AuthorizationRequest request) throws Exception {
        AuthorizationResponse response = transactionService.authorize(request);
        return ResponseEntity.ok(response);
    }
}
