package com.team.banking.event.events;

public class MoneyWithdrawnEvent {

    private final String accountId;

    private final Double amount;

    private final String transferId;

    public MoneyWithdrawnEvent(String accountId,
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