package com.team.banking.command.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class OpenAccountCommand {

    @TargetAggregateIdentifier
    private final String accountId;

    private final String customerName;

    private final String accountType;

    private final Double initialBalance;

    public OpenAccountCommand(String accountId,
                              String customerName,
                              String accountType,
                              Double initialBalance) {
        this.accountId = accountId;
        this.customerName = customerName;
        this.accountType = accountType;
        this.initialBalance = initialBalance;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getAccountType() {
        return accountType;
    }

    public Double getInitialBalance() {
        return initialBalance;
    }
}