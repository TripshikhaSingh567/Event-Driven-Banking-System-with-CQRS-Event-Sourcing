package com.team.banking.query.dto;

import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String accountId;
    private String transactionType;
    private Double amount;
    private Double balanceAfterTransaction;
    private String relatedAccountId;
    private String transferId;
    private LocalDateTime timestamp;

    public TransactionResponse() {
    }

    public TransactionResponse(
            Long id,
            String accountId,
            String transactionType,
            Double amount,
            Double balanceAfterTransaction,
            String relatedAccountId,
            String transferId,
            LocalDateTime timestamp) {

        this.id = id;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.relatedAccountId = relatedAccountId;
        this.transferId = transferId;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public void setBalanceAfterTransaction(Double balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public String getRelatedAccountId() {
        return relatedAccountId;
    }

    public void setRelatedAccountId(String relatedAccountId) {
        this.relatedAccountId = relatedAccountId;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}