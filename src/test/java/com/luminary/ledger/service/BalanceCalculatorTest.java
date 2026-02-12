package com.luminary.ledger.service;

import com.luminary.ledger.domain.enums.AccountType;
import com.luminary.ledger.domain.enums.EntryType;
import com.luminary.ledger.persistence.repository.TransactionEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceCalculatorTest {

    @Mock
    private TransactionEntryRepository entryRepository;

    private BalanceCalculator balanceCalculator;

    @BeforeEach
    void setUp() {
        balanceCalculator = new BalanceCalculator(entryRepository);
    }

    @Test
    void asset_debitsGreaterThanCredits() {
        stubEntries(1L, new BigDecimal("1000.0000"), new BigDecimal("300.0000"));

        BigDecimal balance = balanceCalculator.calculateBalance(1L, AccountType.ASSET);

        assertEquals(new BigDecimal("700.0000"), balance);
    }

    @Test
    void asset_creditsGreaterThanDebits() {
        stubEntries(1L, new BigDecimal("200.0000"), new BigDecimal("500.0000"));

        BigDecimal balance = balanceCalculator.calculateBalance(1L, AccountType.ASSET);

        assertEquals(new BigDecimal("-300.0000"), balance);
    }

    @Test
    void expense_debitsGreaterThanCredits() {
        stubEntries(2L, new BigDecimal("800.0000"), new BigDecimal("150.0000"));

        BigDecimal balance = balanceCalculator.calculateBalance(2L, AccountType.EXPENSE);

        assertEquals(new BigDecimal("650.0000"), balance);
    }

    @Test
    void liability_creditsGreaterThanDebits() {
        stubEntries(3L, new BigDecimal("100.0000"), new BigDecimal("500.0000"));

        BigDecimal balance = balanceCalculator.calculateBalance(3L, AccountType.LIABILITY);

        assertEquals(new BigDecimal("400.0000"), balance);
    }

    @Test
    void revenue_creditsGreaterThanDebits() {
        stubEntries(4L, new BigDecimal("50.0000"), new BigDecimal("1000.0000"));

        BigDecimal balance = balanceCalculator.calculateBalance(4L, AccountType.REVENUE);

        assertEquals(new BigDecimal("950.0000"), balance);
    }

    @Test
    void revenue_debitsGreaterThanCredits() {
        stubEntries(4L, new BigDecimal("600.0000"), new BigDecimal("400.0000"));

        BigDecimal balance = balanceCalculator.calculateBalance(4L, AccountType.REVENUE);

        assertEquals(new BigDecimal("-200.0000"), balance);
    }

    @Test
    void noEntries_returnsZero() {
        stubEntries(5L, BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal balance = balanceCalculator.calculateBalance(5L, AccountType.ASSET);

        assertEquals(BigDecimal.ZERO, balance);
    }

    @Test
    void equalDebitsAndCredits_returnsZero() {
        stubEntries(6L, new BigDecimal("500.0000"), new BigDecimal("500.0000"));

        BigDecimal balance = balanceCalculator.calculateBalance(6L, AccountType.LIABILITY);

        assertEquals(new BigDecimal("0.0000"), balance);
    }

    private void stubEntries(Long accountId, BigDecimal debits, BigDecimal credits) {
        when(entryRepository.sumByAccountIdAndEntryType(accountId, EntryType.DEBIT)).thenReturn(debits);
        when(entryRepository.sumByAccountIdAndEntryType(accountId, EntryType.CREDIT)).thenReturn(credits);
    }
}
