package com.eldar.api.dto;

public record DuplicateTransactionResponse(
        String transactionId,
        boolean wasProcessed) {
}
