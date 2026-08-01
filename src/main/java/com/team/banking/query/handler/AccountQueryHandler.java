package com.team.banking.query.handler;

import com.team.banking.query.dto.AccountResponse;
import com.team.banking.query.entity.AccountEntity;
import com.team.banking.query.queries.GetAccountByIdQuery;
import com.team.banking.query.queries.GetAllAccountsQuery;
import com.team.banking.query.repository.AccountRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountQueryHandler {

    private final AccountRepository accountRepository;

    public AccountQueryHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @QueryHandler
    public AccountResponse handle(GetAccountByIdQuery query) {

        AccountEntity account = accountRepository.findById(query.getAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        return new AccountResponse(
                account.getAccountId(),
                account.getCustomerName(),
                account.getAccountType(),
                account.getBalance()
        );
    }

    @QueryHandler
    public List<AccountResponse> handle(GetAllAccountsQuery query) {

        return accountRepository.findAll()
                .stream()
                .map(account -> new AccountResponse(
                        account.getAccountId(),
                        account.getCustomerName(),
                        account.getAccountType(),
                        account.getBalance()
                ))
                .collect(Collectors.toList());
    }
}