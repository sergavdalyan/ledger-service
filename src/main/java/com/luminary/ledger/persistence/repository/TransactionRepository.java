package com.luminary.ledger.persistence.repository;

import com.luminary.ledger.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    @Query("SELECT DISTINCT t.id FROM TransactionEntity t JOIN t.entries e WHERE e.account.id = :accountId")
    Page<Long> findTransactionIdsByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("SELECT DISTINCT t FROM TransactionEntity t JOIN FETCH t.entries WHERE t.id IN :ids ORDER BY t.createdAt DESC")
    List<TransactionEntity> findAllWithEntriesByIds(@Param("ids") List<Long> ids);
}
