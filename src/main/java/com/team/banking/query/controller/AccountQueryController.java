package com.team.banking.query.controller;

import com.team.banking.query.dto.AccountResponse;
import com.team.banking.query.entity.TransactionEntity;
import com.team.banking.query.queries.GetAccountByIdQuery;
import com.team.banking.query.queries.GetAllAccountsQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.team.banking.query.dto.TransactionResponse;
import com.team.banking.query.queries.GetTransactionHistoryQuery;

@RestController
@RequestMapping("/api/accounts")
public class AccountQueryController {

    private final QueryGateway queryGateway;

    public AccountQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/{accountId}")
    public CompletableFuture<AccountResponse> getAccountById(
            @PathVariable String accountId) {

        return queryGateway.query(
                new GetAccountByIdQuery(accountId),
                ResponseTypes.instanceOf(AccountResponse.class)
        );
    }

    @GetMapping
    public CompletableFuture<List<AccountResponse>> getAllAccounts() {

        return queryGateway.query(
                new GetAllAccountsQuery(),
                ResponseTypes.multipleInstancesOf(AccountResponse.class)
        );
    }


    @GetMapping("/{accountId}/transactions")
    public CompletableFuture<List<TransactionResponse>> getTransactionHistory(
            @PathVariable String accountId) {

        return queryGateway.query(
                new GetTransactionHistoryQuery(accountId),
                ResponseTypes.multipleInstancesOf(TransactionEntity.class)
        ).thenApply(transactions ->
                transactions.stream()
                        .map(transaction ->
                                new TransactionResponse(
                                        transaction.getId(),
                                        transaction.getAccountId(),
                                        transaction.getTransactionType(),
                                        transaction.getAmount(),
                                        transaction.getBalanceAfterTransaction(),
                                        transaction.getRelatedAccountId(),
                                        transaction.getTransferId(),
                                        transaction.getTimestamp()
                                )
                        )
                        .toList()
        );
    }
}