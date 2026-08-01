package com.team.banking.query.queries;

public class GetAccountByIdQuery {

    private final String accountId;

    public GetAccountByIdQuery(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}