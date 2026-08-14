package com.team.banking.query.projection;

import com.team.banking.event.events.AccountOpenedEvent;
import com.team.banking.event.events.MoneyDepositedEvent;
import com.team.banking.event.events.MoneyWithdrawnEvent;
import com.team.banking.event.events.TransferCompletedEvent;
import com.team.banking.event.events.TransferStartedEvent;
import com.team.banking.query.entity.AccountEntity;
import com.team.banking.query.entity.TransactionEntity;
import com.team.banking.query.repository.AccountRepository;
import com.team.banking.query.repository.TransactionRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import com.team.banking.event.events.AccountClosedEvent;


import java.time.LocalDateTime;

@Component
public class TransactionProjection {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionProjection(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    // ---------------- ACCOUNT OPENED ----------------

    @EventHandler
    public void on(AccountOpenedEvent event) {

        TransactionEntity transaction =
                new TransactionEntity(
                        event.getAccountId(),
                        "ACCOUNT_OPENED",
                        event.getInitialBalance(),
                        event.getInitialBalance(),
                        null,
                        null,
                        LocalDateTime.now()
                );

        transactionRepository.save(transaction);
    }

    // ---------------- MONEY DEPOSITED ----------------

    @EventHandler
    public void on(MoneyDepositedEvent event) {

        AccountEntity account =
                accountRepository.findById(event.getAccountId())
                        .orElseThrow(() ->
                                new RuntimeException("Account not found"));

        double balanceAfterTransaction =
                account.getBalance() + event.getAmount();

        TransactionEntity transaction =
                new TransactionEntity(
                        event.getAccountId(),
                        event.getTransferId() == null
                                ? "DEPOSIT"
                                : "TRANSFER_IN",
                        event.getAmount(),
                        balanceAfterTransaction,
                        null,
                        event.getTransferId(),
                        LocalDateTime.now()
                );

        transactionRepository.save(transaction);
    }

    // ---------------- MONEY WITHDRAWN ----------------

    @EventHandler
    public void on(MoneyWithdrawnEvent event) {

        AccountEntity account =
                accountRepository.findById(event.getAccountId())
                        .orElseThrow(() ->
                                new RuntimeException("Account not found"));

        double balanceAfterTransaction =
                account.getBalance() - event.getAmount();

        TransactionEntity transaction =
                new TransactionEntity(
                        event.getAccountId(),
                        event.getTransferId() == null
                                ? "WITHDRAW"
                                : "TRANSFER_OUT",
                        event.getAmount(),
                        balanceAfterTransaction,
                        null,
                        event.getTransferId(),
                        LocalDateTime.now()
                );

        transactionRepository.save(transaction);
    }

    // ---------------- ACCOUNT CLOSED ----------------

    @EventHandler
    public void on(AccountClosedEvent event) {

        AccountEntity account =
                accountRepository.findById(event.getAccountId())
                        .orElseThrow(() ->
                                new RuntimeException("Account not found"));

        TransactionEntity transaction =
                new TransactionEntity(
                        event.getAccountId(),
                        "ACCOUNT_CLOSED",
                        0.0,
                        account.getBalance(),
                        null,
                        null,
                        LocalDateTime.now()
                );

        transactionRepository.save(transaction);

        System.out.println(
                "Account closure recorded in transaction history: "
                        + account.getAccountId()
        );
    }

    // ---------------- TRANSFER STARTED ----------------

    @EventHandler
    public void on(TransferStartedEvent event) {

        // TransferStartedEvent does not change the account balance.
        // Actual transaction records are created when withdrawal
        // and deposit events are processed.
    }

    // ---------------- TRANSFER COMPLETED ----------------

    @EventHandler
    public void on(TransferCompletedEvent event) {

        // TransferCompletedEvent is used to complete the transfer saga.
        // The actual account transaction records are already created
        // by MoneyWithdrawnEvent and MoneyDepositedEvent.
    }
}