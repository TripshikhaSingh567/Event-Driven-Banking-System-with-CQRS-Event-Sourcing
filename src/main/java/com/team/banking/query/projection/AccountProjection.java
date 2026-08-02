package com.team.banking.query.projection;

import com.team.banking.event.events.AccountOpenedEvent;
import com.team.banking.query.entity.AccountEntity;
import com.team.banking.query.repository.AccountRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import com.team.banking.event.events.MoneyDepositedEvent;

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
                event.getInitialBalance()
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
}