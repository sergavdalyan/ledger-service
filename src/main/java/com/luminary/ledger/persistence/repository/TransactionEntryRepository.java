package com.luminary.ledger.persistence.repository;

import com.luminary.ledger.domain.enums.EntryType;
import com.luminary.ledger.persistence.entity.TransactionEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntryEntity, Long> {

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM TransactionEntryEntity e " +
            "WHERE e.account.id = :accountId AND e.entryType = :entryType")
    BigDecimal sumByAccountIdAndEntryType(@Param("accountId") Long accountId,
                                          @Param("entryType") EntryType entryType);
}
