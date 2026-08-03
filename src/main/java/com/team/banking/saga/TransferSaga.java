package com.team.banking.saga;

import com.team.banking.command.commands.CompleteTransferCommand;
import com.team.banking.command.commands.DepositMoneyCommand;
import com.team.banking.command.commands.WithdrawMoneyCommand;
import com.team.banking.event.events.MoneyDepositedEvent;
import com.team.banking.event.events.MoneyWithdrawnEvent;
import com.team.banking.event.events.TransferStartedEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.*;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import com.team.banking.event.events.TransferCompletedEvent;

@Saga
public class TransferSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    private String sourceAccountId;
    private String transferId;
    private String destinationAccountId;
    private Double amount;

    @StartSaga
    @SagaEventHandler(
            associationProperty = "transferId"
    )
    public void on(TransferStartedEvent event) {

        this.sourceAccountId = event.getSourceAccountId();
        this.transferId = event.getTransferId();
        this.destinationAccountId = event.getDestinationAccountId();
        this.amount = event.getAmount();

        commandGateway.sendAndWait(
                new WithdrawMoneyCommand(
                        sourceAccountId,
                        amount,
                        transferId
                )
        );
    }

    @SagaEventHandler(
            associationProperty = "transferId"
    )
    public void on(MoneyWithdrawnEvent event){
        commandGateway.sendAndWait(
                new DepositMoneyCommand(
                        destinationAccountId,
                        amount,
                        transferId
                )
        );
    }

    @SagaEventHandler(
            associationProperty = "transferId"
    )
    public void on(MoneyDepositedEvent event){

        commandGateway.sendAndWait(
                new CompleteTransferCommand(
                        transferId
                )
        );
    }

    @EndSaga
    @SagaEventHandler(
            associationProperty = "transferId"
    )
    public void on(TransferCompletedEvent event) {

        System.out.println(
                "Transfer Completed Successfully. Transfer ID: "
                        + event.getTransferId()
        );
    }
}