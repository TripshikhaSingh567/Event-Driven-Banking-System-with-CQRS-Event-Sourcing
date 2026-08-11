package com.team.banking.query.handler;

import com.team.banking.query.entity.TransactionEntity;
import com.team.banking.query.queries.GetTransactionHistoryQuery;
import com.team.banking.query.repository.TransactionRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionQueryHandler {

    private final TransactionRepository transactionRepository;

    public TransactionQueryHandler(
            TransactionRepository transactionRepository) {

        this.transactionRepository = transactionRepository;
    }

    @QueryHandler
    public List<TransactionEntity> handle(
            GetTransactionHistoryQuery query) {

        return transactionRepository
                .findByAccountIdOrderByTimestampDesc(
                        query.getAccountId()
                );
    }
}