package com.eldar.api.repository;

import com.eldar.api.dto.TopAccountsResponse;
import com.eldar.api.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    boolean existsByTransactionId(String transactionId);

    List<Transaction> findByAccountId(String accountId);

    long countByAccountId(String accountId);

    List<Transaction> findByProcessingId(UUID processingId);

    @Query("""
        SELECT 
            COALESCE(
                SUM(
                    CASE 
                        WHEN t.type = 'CREDIT' THEN t.amount
                        WHEN t.type = 'DEBIT' THEN -t.amount
                    END
                ),0
            )
        FROM Transaction t
        WHERE t.accountId = :accountId
    """)
    BigDecimal getBalanceByAccountId(String accountId);


    @Query("""
        SELECT new com.eldar.api.dto.TopAccountsResponse(
            t.accountId,
            COUNT(t)
        )
        FROM Transaction t
        GROUP BY t.accountId
        ORDER BY COUNT(t) DESC
    """)
    List<TopAccountsResponse> findTopAccounts(Pageable pageable);
}
