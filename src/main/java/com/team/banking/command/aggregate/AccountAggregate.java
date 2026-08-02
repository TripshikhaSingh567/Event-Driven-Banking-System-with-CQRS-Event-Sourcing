package com.team.banking.command.aggregate;

import com.team.banking.command.commands.OpenAccountCommand;
import com.team.banking.event.events.AccountOpenedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import com.team.banking.command.commands.DepositMoneyCommand;
import com.team.banking.event.events.MoneyDepositedEvent;

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
}