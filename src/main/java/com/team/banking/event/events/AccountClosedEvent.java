package com.team.banking.event.events;

public class AccountClosedEvent {

    private final String accountId;

    public AccountClosedEvent(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}