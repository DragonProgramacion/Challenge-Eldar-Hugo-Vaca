package com.eldar.api.service.imp;

import com.eldar.api.enums.TransactionType;
import com.eldar.api.entity.AccountBalance;
import com.eldar.api.entity.Transaction;
import com.eldar.api.repository.AccountRepository;
import com.eldar.api.repository.TransactionRepository;
import com.eldar.api.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImp implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public void saveTransaction(Transaction transaction, UUID processingId) {

        transaction.setProcessingId(processingId);

        if (transactionRepository.existsByTransactionId(transaction.getTransactionId())) {
            throw new IllegalArgumentException(
                    "Duplicated transaction: " + transaction.getTransactionId());
        }

        transactionRepository.save(transaction);

        updateAccountBalance(transaction);
    }

    @Override
    public Boolean isTransactionProcessed(String transactionId) {
        return transactionRepository.existsByTransactionId(transactionId);
    }

    private void updateAccountBalance(Transaction transaction) {

        AccountBalance balance = accountRepository
                .findById(transaction.getAccountId())
                .orElseGet(() -> createAccountBalance(transaction.getAccountId()));

        BigDecimal amount = transaction.getAmount();

        if (transaction.getType() == TransactionType.DEBIT) {
            balance.setBalance(balance.getBalance().subtract(amount));
        } else {
            balance.setBalance(balance.getBalance().add(amount));
        }

        balance.setTransactionCount(balance.getTransactionCount() + 1);
        balance.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(balance);
    }

    private AccountBalance createAccountBalance(String accountId) {

        AccountBalance balance = new AccountBalance();
        balance.setAccountId(accountId);
        balance.setBalance(BigDecimal.ZERO);
        balance.setTransactionCount(0L);
        balance.setUpdatedAt(LocalDateTime.now());

        return balance;
    }
}

