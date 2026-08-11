package com.team.banking.query.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountId;

    private String transactionType;

    private Double amount;

    private Double balanceAfterTransaction;

    private String relatedAccountId;

    private String transferId;

    private LocalDateTime timestamp;

    public TransactionEntity() {
    }

    public TransactionEntity(
            String accountId,
            String transactionType,
            Double amount,
            Double balanceAfterTransaction,
            String relatedAccountId,
            String transferId,
            LocalDateTime timestamp) {

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

    public String getAccountId() {
        return accountId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Double getAmount() {
        return amount;
    }

    public Double getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public String getRelatedAccountId() {
        return relatedAccountId;
    }

    public String getTransferId() {
        return transferId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setBalanceAfterTransaction(Double balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public void setRelatedAccountId(String relatedAccountId) {
        this.relatedAccountId = relatedAccountId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}