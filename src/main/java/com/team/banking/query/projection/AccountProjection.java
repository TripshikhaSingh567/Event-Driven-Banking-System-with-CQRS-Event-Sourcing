package com.team.banking.query.projection;

import com.team.banking.event.events.AccountOpenedEvent;
import com.team.banking.event.events.MoneyDepositedEvent;
import com.team.banking.event.events.MoneyWithdrawnEvent;
import com.team.banking.query.entity.AccountEntity;
import com.team.banking.query.repository.AccountRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import com.team.banking.event.events.AccountClosedEvent;

@Component
public class AccountProjection {

    private final AccountRepository accountRepository;

    public AccountProjection(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @EventHandler
    public void on(AccountOpenedEvent event) {

        AccountEntity account = new AccountEntity(
                event.getAccountId(),
                event.getCustomerName(),
                event.getAccountType(),
                event.getInitialBalance(),
                "ACTIVE"
        );

        accountRepository.save(account);

        System.out.println("Account saved in Read Database: " + account.getAccountId());
    }

    @EventHandler
    public void on(MoneyDepositedEvent event) {

        AccountEntity account = accountRepository.findById(event.getAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        account.setBalance(
                account.getBalance() + event.getAmount()
        );

        accountRepository.save(account);

        System.out.println(
                "Money deposited into account: " + account.getAccountId()
        );
    }

    @EventHandler
    public void on(MoneyWithdrawnEvent event) {

        AccountEntity account = accountRepository.findById(event.getAccountId())
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        account.setBalance(
                account.getBalance() - event.getAmount()
        );

        accountRepository.save(account);

        System.out.println(
                "Money withdrawn from account: " + account.getAccountId()
        );
    }


    @EventHandler
    public void on(AccountClosedEvent event) {

        AccountEntity account =
                accountRepository.findById(event.getAccountId())
                        .orElseThrow(() ->
                                new RuntimeException("Account not found"));

        account.setStatus("CLOSED");

        accountRepository.save(account);

        System.out.println(
                "Account closed in Read Database: "
                        + account.getAccountId()
        );
    }
}