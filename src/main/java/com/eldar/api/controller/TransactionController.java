package com.eldar.api.controller;

import com.eldar.api.dto.DuplicateTransactionResponse;
import com.eldar.api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Transactions", description = "Operations related to transactions")
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    TransactionService transactionService;


    @Operation(
            summary = "Check if a transaction was already processed",
            description = "Returns whether a transactionId has already been processed by the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/{transactionId}/status")
    public ResponseEntity<DuplicateTransactionResponse> isTransactionProcessed(
            @Parameter(description = "Unique identifier of the transaction", example = "t123")
            @PathVariable String transactionId) {

        boolean duplicate = transactionService.isTransactionProcessed(transactionId);

        DuplicateTransactionResponse response =
                new DuplicateTransactionResponse(transactionId, duplicate);

        return ResponseEntity.ok(response);
    }

}