package com.eldar.api.service.imp;

import com.eldar.api.dto.TopAccountsResponse;
import com.eldar.api.entity.AccountBalance;
import com.eldar.api.repository.AccountRepository;
import com.eldar.api.repository.TransactionRepository;
import com.eldar.api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImp implements AccountService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public BigDecimal getBalanceByAccountId(String accountId) {
        return accountRepository.findById(accountId)
                .map(AccountBalance::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public List<TopAccountsResponse> getTopAccounts(int limit) {
        return transactionRepository.findTopAccounts(PageRequest.of(0, limit));
    }
}
