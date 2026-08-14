package com.team.banking.command.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CloseAccountCommand {

    @TargetAggregateIdentifier
    private final String accountId;

    public CloseAccountCommand(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}