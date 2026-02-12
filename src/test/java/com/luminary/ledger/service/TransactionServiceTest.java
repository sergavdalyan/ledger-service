package com.luminary.ledger.service;

import com.luminary.ledger.domain.enums.AccountType;
import com.luminary.ledger.domain.enums.EntryType;
import com.luminary.ledger.domain.exception.AccountNotFoundException;
import com.luminary.ledger.domain.exception.TransactionNotFoundException;
import com.luminary.ledger.domain.model.Transaction;
import com.luminary.ledger.domain.model.TransactionEntry;
import com.luminary.ledger.domain.vo.Money;
import com.luminary.ledger.persistence.entity.AccountEntity;
import com.luminary.ledger.persistence.entity.TransactionEntity;
import com.luminary.ledger.persistence.entity.TransactionEntryEntity;
import com.luminary.ledger.persistence.mapper.TransactionEntityMapper;
import com.luminary.ledger.persistence.repository.AccountRepository;
import com.luminary.ledger.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionEntityMapper transactionMapper;

    private TransactionService transactionService;

    private AccountEntity assetAccount;
    private AccountEntity revenueAccount;
    private Transaction balancedTransaction;
    private TransactionEntity transactionEntity;
    private Transaction savedTransaction;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, accountRepository, transactionMapper);

        LocalDateTime now = LocalDateTime.now();

        assetAccount = new AccountEntity();
        assetAccount.setId(1L);
        assetAccount.setName("Cash");
        assetAccount.setType(AccountType.ASSET);
        assetAccount.setCreatedAt(now);
        assetAccount.setUpdatedAt(now);

        revenueAccount = new AccountEntity();
        revenueAccount.setId(2L);
        revenueAccount.setName("Revenue");
        revenueAccount.setType(AccountType.REVENUE);
        revenueAccount.setCreatedAt(now);
        revenueAccount.setUpdatedAt(now);

        List<TransactionEntry> entries = List.of(
                TransactionEntry.create(1L, EntryType.DEBIT, Money.of("100")),
                TransactionEntry.create(2L, EntryType.CREDIT, Money.of("100"))
        );
        balancedTransaction = Transaction.create("Test sale", now, entries);

        transactionEntity = new TransactionEntity();
        transactionEntity.setId(10L);
        transactionEntity.setDescription("Test sale");
        transactionEntity.setDate(now);
        transactionEntity.setCreatedAt(now);

        savedTransaction = new Transaction(10L, "Test sale", now, entries, now);
    }

    @Test
    void createTransaction_success() {
        when(accountRepository.findAllById(any())).thenReturn(List.of(assetAccount, revenueAccount));
        when(transactionMapper.toEntity(any(Transaction.class), anyMap())).thenReturn(transactionEntity);
        when(transactionRepository.save(transactionEntity)).thenReturn(transactionEntity);
        when(transactionMapper.toDomain(transactionEntity)).thenReturn(savedTransaction);

        Transaction result = transactionService.createTransaction(balancedTransaction);

        assertEquals(10L, result.getId());
        assertEquals("Test sale", result.getDescription());
        verify(accountRepository).findAllById(any());
        verify(transactionRepository).save(transactionEntity);
    }

    @Test
    void createTransaction_accountNotFound_throwsException() {
        when(accountRepository.findAllById(any())).thenReturn(List.of(assetAccount));

        assertThrows(AccountNotFoundException.class,
                () -> transactionService.createTransaction(balancedTransaction));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_noAccountsFound_throwsException() {
        when(accountRepository.findAllById(any())).thenReturn(List.of());

        assertThrows(AccountNotFoundException.class,
                () -> transactionService.createTransaction(balancedTransaction));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransaction_found() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transactionEntity));
        when(transactionMapper.toDomain(transactionEntity)).thenReturn(savedTransaction);

        Transaction result = transactionService.getTransaction(10L);

        assertEquals(10L, result.getId());
        assertEquals("Test sale", result.getDescription());
    }

    @Test
    void getTransaction_notFound_throwsException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransaction(99L));
    }

    @Test
    void getTransactionsByAccountId_returnsTransactions() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Long> idPage = new PageImpl<>(List.of(10L), pageable, 1);
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findTransactionIdsByAccountId(1L, pageable)).thenReturn(idPage);
        when(transactionRepository.findAllWithEntriesByIds(List.of(10L))).thenReturn(List.of(transactionEntity));
        when(transactionMapper.toDomain(transactionEntity)).thenReturn(savedTransaction);

        Page<Transaction> result = transactionService.getTransactionsByAccountId(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test sale", result.getContent().get(0).getDescription());
    }

    @Test
    void getTransactionsByAccountId_accountNotFound_throwsException() {
        Pageable pageable = PageRequest.of(0, 20);
        when(accountRepository.existsById(99L)).thenReturn(false);

        assertThrows(AccountNotFoundException.class,
                () -> transactionService.getTransactionsByAccountId(99L, pageable));

        verify(transactionRepository, never()).findTransactionIdsByAccountId(any(), any());
    }

    @Test
    void getTransactionsByAccountId_noTransactions_returnsEmptyList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Long> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findTransactionIdsByAccountId(1L, pageable)).thenReturn(emptyPage);

        Page<Transaction> result = transactionService.getTransactionsByAccountId(1L, pageable);

        assertTrue(result.isEmpty());
        verify(transactionRepository, never()).findAllWithEntriesByIds(any());
    }
}
