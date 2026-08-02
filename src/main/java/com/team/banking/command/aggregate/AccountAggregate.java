package com.team.banking.command.aggregate;

import com.team.banking.command.commands.DepositMoneyCommand;
import com.team.banking.command.commands.OpenAccountCommand;
import com.team.banking.command.commands.WithdrawMoneyCommand;
import com.team.banking.event.events.AccountOpenedEvent;
import com.team.banking.event.events.MoneyDepositedEvent;
import com.team.banking.event.events.MoneyWithdrawnEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class AccountAggregate {

    @AggregateIdentifier
    private String accountId;

    private String customerName;

    private String accountType;

    private Double balance;

    // Required by Axon
    public AccountAggregate() {
    }

    // -------------------- OPEN ACCOUNT --------------------

    @CommandHandler
    public AccountAggregate(OpenAccountCommand command) {

        // Business Validation
        if (command.getInitialBalance() < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        AggregateLifecycle.apply(
                new AccountOpenedEvent(
                        command.getAccountId(),
                        command.getCustomerName(),
                        command.getAccountType(),
                        command.getInitialBalance()
                )
        );
    }

    // -------------------- DEPOSIT MONEY --------------------

    @CommandHandler
    public void handle(DepositMoneyCommand command) {

        // Business Validation
        if (command.getAmount() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        AggregateLifecycle.apply(
                new MoneyDepositedEvent(
                        command.getAccountId(),
                        command.getAmount()
                )
        );
    }

    // -------------------- WITHDRAW MONEY --------------------

    @CommandHandler
    public void handle(WithdrawMoneyCommand command) {

        // Business Validation
        if (command.getAmount() <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        if (balance < command.getAmount()) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        AggregateLifecycle.apply(
                new MoneyWithdrawnEvent(
                        command.getAccountId(),
                        command.getAmount()
                )
        );
    }

    // -------------------- EVENT SOURCING HANDLERS --------------------

    @EventSourcingHandler
    public void on(AccountOpenedEvent event) {

        this.accountId = event.getAccountId();
        this.customerName = event.getCustomerName();
        this.accountType = event.getAccountType();
        this.balance = event.getInitialBalance();
    }

    @EventSourcingHandler
    public void on(MoneyDepositedEvent event) {

        this.balance += event.getAmount();
    }

    @EventSourcingHandler
    public void on(MoneyWithdrawnEvent event) {

        this.balance -= event.getAmount();
    }
}