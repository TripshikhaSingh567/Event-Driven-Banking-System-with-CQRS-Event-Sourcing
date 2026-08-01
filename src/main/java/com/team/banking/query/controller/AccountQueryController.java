package com.team.banking.query.controller;

import com.team.banking.query.dto.AccountResponse;
import com.team.banking.query.queries.GetAccountByIdQuery;
import com.team.banking.query.queries.GetAllAccountsQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
}