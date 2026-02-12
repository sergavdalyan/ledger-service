package com.luminary.ledger.persistence.mapper;

import com.luminary.ledger.domain.model.Transaction;
import com.luminary.ledger.domain.model.TransactionEntry;
import com.luminary.ledger.domain.vo.Money;
import com.luminary.ledger.persistence.entity.AccountEntity;
import com.luminary.ledger.persistence.entity.TransactionEntity;
import com.luminary.ledger.persistence.entity.TransactionEntryEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TransactionEntityMapper {

    public Transaction toDomain(TransactionEntity entity) {
        List<TransactionEntry> entries = entity.getEntries().stream()
                .map(this::entryToDomain)
                .toList();

        return new Transaction(
                entity.getId(),
                entity.getDescription(),
                entity.getDate(),
                entries,
                entity.getCreatedAt()
        );
    }

    public TransactionEntity toEntity(Transaction domain, Map<Long, AccountEntity> accountEntities) {
        TransactionEntity entity = new TransactionEntity();
        entity.setDescription(domain.getDescription());
        entity.setDate(domain.getDate());
        entity.setCreatedAt(domain.getCreatedAt());

        List<TransactionEntryEntity> entryEntities = domain.getEntries().stream()
                .map(entry -> {
                    TransactionEntryEntity entryEntity = new TransactionEntryEntity();
                    entryEntity.setTransaction(entity);
                    entryEntity.setAccount(accountEntities.get(entry.getAccountId()));
                    entryEntity.setEntryType(entry.getEntryType());
                    entryEntity.setAmount(entry.getAmount().value());
                    entryEntity.setCreatedAt(entry.getCreatedAt());
                    return entryEntity;
                })
                .toList();

        entity.setEntries(entryEntities);
        return entity;
    }

    private TransactionEntry entryToDomain(TransactionEntryEntity entity) {
        return new TransactionEntry(
                entity.getId(),
                entity.getAccount().getId(),
                entity.getEntryType(),
                Money.of(entity.getAmount()),
                entity.getCreatedAt()
        );
    }
}
