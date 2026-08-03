package com.team.banking.command.aggregate;

import com.team.banking.command.commands.TransferMoneyCommand;
import com.team.banking.event.events.TransferStartedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import com.team.banking.command.commands.CompleteTransferCommand;
import com.team.banking.event.events.TransferCompletedEvent;

@Aggregate
public class TransferAggregate {

    @AggregateIdentifier
    private String transferId;

    private String sourceAccountId;

    private String destinationAccountId;

    private Double amount;

    private boolean completed;

    // Required by Axon
    public TransferAggregate() {
    }

    @CommandHandler
    public TransferAggregate(TransferMoneyCommand command) {

        AggregateLifecycle.apply(
                new TransferStartedEvent(
                        command.getTransferId(),
                        command.getSourceAccountId(),
                        command.getDestinationAccountId(),
                        command.getAmount()
                )
        );
    }

    @CommandHandler
    public void handle(CompleteTransferCommand command) {

        if (completed) {
            throw new IllegalStateException("Transfer already completed");
        }

        AggregateLifecycle.apply(
                new TransferCompletedEvent(
                        transferId,
                        sourceAccountId,
                        destinationAccountId,
                        amount
                )
        );
    }

    @EventSourcingHandler
    public void on(TransferStartedEvent event) {

        this.transferId = event.getTransferId();
        this.sourceAccountId = event.getSourceAccountId();
        this.destinationAccountId = event.getDestinationAccountId();
        this.amount = event.getAmount();
    }

    @EventSourcingHandler
    public void on(TransferCompletedEvent event) {

        this.completed = true;
    }
}