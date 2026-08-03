package com.team.banking.command.controller;

import com.team.banking.command.commands.TransferMoneyCommand;
import com.team.banking.command.dto.TransferMoneyRequest;
import jakarta.validation.Valid;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class TransferController {

    private final CommandGateway commandGateway;

    public TransferController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping("/{sourceAccountId}/transfer")
    public String transferMoney(
            @PathVariable String sourceAccountId,
            @Valid @RequestBody TransferMoneyRequest request) {

        String transferId = UUID.randomUUID().toString();

        TransferMoneyCommand command =
                new TransferMoneyCommand(
                        transferId,
                        sourceAccountId,
                        request.getDestinationAccountId(),
                        request.getAmount()
                );

        commandGateway.sendAndWait(command);

        return "Transfer request submitted successfully. Transfer ID: " + transferId;
    }
}