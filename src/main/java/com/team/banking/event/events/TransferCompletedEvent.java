package com.team.banking.event.events;

public class TransferCompletedEvent {

    private final String transferId;

    private final String sourceAccountId;

    private final String destinationAccountId;

    private final Double amount;

    public TransferCompletedEvent(String transferId,
                                  String sourceAccountId,
                                  String destinationAccountId,
                                  Double amount) {

        this.transferId = transferId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }

    public String getTransferId() {
        return transferId;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public Double getAmount() {
        return amount;
    }
}