package com.luminary.ledger.service;

import com.luminary.ledger.domain.exception.AccountNotFoundException;
import com.luminary.ledger.domain.exception.TransactionNotFoundException;
import com.luminary.ledger.domain.model.Transaction;
import com.luminary.ledger.domain.model.TransactionEntry;
import com.luminary.ledger.persistence.entity.AccountEntity;
import com.luminary.ledger.persistence.entity.TransactionEntity;
import com.luminary.ledger.persistence.mapper.TransactionEntityMapper;
import com.luminary.ledger.persistence.repository.AccountRepository;
import com.luminary.ledger.persistence.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionEntityMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              TransactionEntityMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        Set<Long> accountIds = transaction.getEntries().stream()
                .map(TransactionEntry::getAccountId)
                .collect(Collectors.toSet());

        Map<Long, AccountEntity> accountEntities = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(AccountEntity::getId, a -> a));

        for (Long accountId : accountIds) {
            if (!accountEntities.containsKey(accountId)) {
                throw new AccountNotFoundException(accountId);
            }
        }

        TransactionEntity entity = transactionMapper.toEntity(transaction, accountEntities);
        TransactionEntity saved = transactionRepository.save(entity);
        return transactionMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    public Transaction getTransaction(Long id) {
        TransactionEntity entity = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        return transactionMapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        Page<Long> transactionIds = transactionRepository.findTransactionIdsByAccountId(accountId, pageable);
        if (transactionIds.isEmpty()) {
            return transactionIds.map(id -> null);
        }
        List<TransactionEntity> entities = transactionRepository.findAllWithEntriesByIds(transactionIds.getContent());
        Map<Long, Transaction> transactionMap = entities.stream()
                .collect(Collectors.toMap(TransactionEntity::getId, transactionMapper::toDomain));
        return transactionIds.map(transactionMap::get);
    }
}
