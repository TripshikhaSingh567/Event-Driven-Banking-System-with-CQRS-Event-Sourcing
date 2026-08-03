package com.team.banking.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransferMoneyRequest {

    @NotBlank(message = "Destination Account Id is required")
    private String destinationAccountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

    public TransferMoneyRequest() {
    }

    public TransferMoneyRequest(String destinationAccountId, Double amount) {
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}