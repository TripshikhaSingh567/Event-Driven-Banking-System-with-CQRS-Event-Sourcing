package com.team.banking.query.repository;

import com.team.banking.query.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository
        extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findByAccountIdOrderByTimestampDesc(String accountId);
}