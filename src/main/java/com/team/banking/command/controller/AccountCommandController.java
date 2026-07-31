package com.team.banking.command.controller;

import com.team.banking.command.commands.OpenAccountCommand;
import com.team.banking.command.dto.OpenAccountRequest;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountCommandController {

    private final CommandGateway commandGateway;

    public AccountCommandController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public String openAccount(@RequestBody OpenAccountRequest request) {

        String accountId = UUID.randomUUID().toString();

        OpenAccountCommand command =
                new OpenAccountCommand(
                        accountId,
                        request.getCustomerName(),
                        request.getAccountType(),
                        request.getInitialBalance()
                );

        commandGateway.sendAndWait(command);

        return "Account created successfully. Account ID: " + accountId;
    }
}