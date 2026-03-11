package com.eldar.api.controller;

import com.eldar.api.dto.TopAccountsResponse;
import com.eldar.api.service.AccountService;
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

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Accounts", description = "Operations related to accounts and balances")
@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Operation(
            summary = "Get account balance",
            description = "Returns the current balance of a specific account calculated from processed transactions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}/balance")
    public BigDecimal getBalanceByAccountId(
            @Parameter(description = "Account identifier", example = "acc1")
            @PathVariable String accountId) {
        BigDecimal balance = accountService.getBalanceByAccountId(accountId);

        return ResponseEntity.ok(balance).getBody();
    }

    @Operation(
            summary = "Get top 10 accounts",
            description = "Returns the top 10 accounts with the highest number of processed transactions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top accounts retrieved successfully")
    })
    @GetMapping("/top-accounts")
    public List<TopAccountsResponse> getTopAccounts() {
        List<TopAccountsResponse> response = accountService.getTopAccounts();

        return ResponseEntity.ok(response).getBody();
    }
}