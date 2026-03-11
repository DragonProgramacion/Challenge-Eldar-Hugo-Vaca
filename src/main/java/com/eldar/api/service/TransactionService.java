package com.eldar.api.service;

import com.eldar.api.entity.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    void saveTransaction(Transaction transaction, UUID processingId);

    int saveBatch(List<Transaction> transactions, UUID processingId);

    Boolean isTransactionProcessed(String transactionId);
}
