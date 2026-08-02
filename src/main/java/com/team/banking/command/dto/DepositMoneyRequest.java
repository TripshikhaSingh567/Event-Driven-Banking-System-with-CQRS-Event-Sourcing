package com.team.banking.command.dto;

public class DepositMoneyRequest {

    private Double amount;

    public DepositMoneyRequest() {
    }

    public DepositMoneyRequest(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}