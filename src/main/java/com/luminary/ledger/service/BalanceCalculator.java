package com.luminary.ledger.service;

import com.luminary.ledger.domain.enums.AccountType;
import com.luminary.ledger.domain.enums.EntryType;
import com.luminary.ledger.domain.vo.Money;
import com.luminary.ledger.persistence.repository.TransactionEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BalanceCalculator {

    private final TransactionEntryRepository entryRepository;

    public BalanceCalculator(TransactionEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    public BigDecimal calculateBalance(Long accountId, AccountType accountType) {
        BigDecimal debits = entryRepository.sumByAccountIdAndEntryType(accountId, EntryType.DEBIT);
        BigDecimal credits = entryRepository.sumByAccountIdAndEntryType(accountId, EntryType.CREDIT);

        if (accountType.isDebitNormal()) {
            return debits.subtract(credits);
        } else {
            return credits.subtract(debits);
        }
    }
}
