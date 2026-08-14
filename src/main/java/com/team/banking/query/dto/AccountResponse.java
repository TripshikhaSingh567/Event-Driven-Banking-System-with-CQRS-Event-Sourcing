package com.team.banking.query.dto;

public class AccountResponse {

    private String accountId;
    private String customerName;
    private String accountType;
    private Double balance;

    private String status;

    public AccountResponse() {
    }

    public AccountResponse(
            String accountId,
            String customerName,
            String accountType,
            Double balance,
            String status) {

        this.accountId = accountId;
        this.customerName = customerName;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}