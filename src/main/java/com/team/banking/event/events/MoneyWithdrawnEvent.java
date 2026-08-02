package com.team.banking.event.events;

public class MoneyWithdrawnEvent {

    private final String accountId;

    private final Double amount;

    public MoneyWithdrawnEvent(String accountId, Double amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public Double getAmount() {
        return amount;
    }
}