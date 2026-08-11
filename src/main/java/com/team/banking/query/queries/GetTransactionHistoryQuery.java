package com.team.banking.query.queries;

public class GetTransactionHistoryQuery {

    private final String accountId;

    public GetTransactionHistoryQuery(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}