package com.team.banking.command.controller;

import com.team.banking.command.commands.DepositMoneyCommand;
import com.team.banking.command.commands.OpenAccountCommand;
import com.team.banking.command.commands.WithdrawMoneyCommand;
import com.team.banking.command.dto.DepositMoneyRequest;
import com.team.banking.command.dto.OpenAccountRequest;
import com.team.banking.command.dto.WithdrawMoneyRequest;
import jakarta.validation.Valid;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.*;
import com.team.banking.command.commands.CloseAccountCommand;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountCommandController {

    private final CommandGateway commandGateway;

    public AccountCommandController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    // ---------------- OPEN ACCOUNT ----------------

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

    // ---------------- DEPOSIT MONEY ----------------

    @PostMapping("/{accountId}/deposit")
    public String depositMoney(
            @PathVariable String accountId,
            @RequestBody DepositMoneyRequest request) {

        DepositMoneyCommand command =
                new DepositMoneyCommand(
                        accountId,
                        request.getAmount(),
                        null
                );

        commandGateway.sendAndWait(command);

        return "Money deposited successfully.";
    }

    // ---------------- WITHDRAW MONEY ----------------

    @PutMapping("/{accountId}/withdraw")
    public String withdrawMoney(
            @PathVariable String accountId,
            @Valid @RequestBody WithdrawMoneyRequest request) {

        WithdrawMoneyCommand command =
                new WithdrawMoneyCommand(
                        accountId,
                        request.getAmount(),
                        null
                );

        commandGateway.sendAndWait(command);

        return "Money withdrawn successfully.";
    }


    // ---------------- CLOSE ACCOUNT ----------------

    @PutMapping("/{accountId}/close")
    public String closeAccount(
            @PathVariable String accountId) {

        CloseAccountCommand command =
                new CloseAccountCommand(accountId);

        commandGateway.sendAndWait(command);

        return "Account closed successfully.";
    }
}