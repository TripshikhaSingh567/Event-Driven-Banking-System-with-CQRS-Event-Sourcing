package com.team.banking.command.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class DepositMoneyCommand {

    @TargetAggregateIdentifier
    private final String accountId;

    private final Double amount;

    public DepositMoneyCommand(String accountId, Double amount) {
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