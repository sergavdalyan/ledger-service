package com.luminary.ledger.api.mapper;

import com.luminary.ledger.api.dto.TransactionEntryResponse;
import com.luminary.ledger.api.dto.TransactionResponse;
import com.luminary.ledger.domain.enums.EntryType;
import com.luminary.ledger.domain.model.Transaction;
import com.luminary.ledger.domain.model.TransactionEntry;
import com.luminary.ledger.domain.vo.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TransactionDtoMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        List<TransactionEntryResponse> entries = transaction.getEntries().stream()
                .map(this::toEntryResponse)
                .toList();

        BigDecimal totalAmount = transaction.getEntries().stream()
                .filter(e -> e.getEntryType() == EntryType.DEBIT)
                .map(TransactionEntry::getAmount)
                .reduce(Money.ZERO, Money::add)
                .value();

        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getDate(),
                entries,
                totalAmount,
                transaction.getCreatedAt()
        );
    }

    private TransactionEntryResponse toEntryResponse(TransactionEntry entry) {
        return new TransactionEntryResponse(
                entry.getId(),
                entry.getAccountId(),
                entry.getEntryType().name(),
                entry.getAmount().value()
        );
    }
}
