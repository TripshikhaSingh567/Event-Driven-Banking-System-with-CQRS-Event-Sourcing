package com.team.banking.command.aggregate;

import com.team.banking.command.commands.OpenAccountCommand;
import com.team.banking.event.events.AccountOpenedEvent;
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

    @EventSourcingHandler
    public void on(AccountOpenedEvent event) {

        this.accountId = event.getAccountId();
        this.customerName = event.getCustomerName();
        this.accountType = event.getAccountType();
        this.balance = event.getInitialBalance();
    }
}