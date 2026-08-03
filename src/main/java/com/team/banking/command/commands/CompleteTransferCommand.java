package com.team.banking.command.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CompleteTransferCommand {

    @TargetAggregateIdentifier
    private final String transferId;

    public CompleteTransferCommand(String transferId) {
        this.transferId = transferId;
    }

    public String getTransferId() {
        return transferId;
    }
}