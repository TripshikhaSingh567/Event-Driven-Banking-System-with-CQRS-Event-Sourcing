package com.team.banking.command.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class DepositMoneyCommand {

    @TargetAggregateIdentifier
    private final String accountId;

    private final Double amount;

    private final String transferId;

    public DepositMoneyCommand(String accountId,
                               Double amount,
                               String transferId) {

        this.accountId = accountId;
        this.amount = amount;
        this.transferId = transferId;
    }

    public String getAccountId() {
        return accountId;
    }

    public Double getAmount() {
        return amount;
    }

    public String getTransferId() {
        return transferId;
    }
}