package com.eldar.api.service;

import com.eldar.api.dto.TopAccountsResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    BigDecimal getBalanceByAccountId(String accountId);

    List<TopAccountsResponse> getTopAccounts(int limit);
}
