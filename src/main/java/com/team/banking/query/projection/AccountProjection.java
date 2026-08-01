package com.team.banking.query.projection;

import com.team.banking.event.events.AccountOpenedEvent;
import com.team.banking.query.entity.AccountEntity;
import com.team.banking.query.repository.AccountRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

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
}