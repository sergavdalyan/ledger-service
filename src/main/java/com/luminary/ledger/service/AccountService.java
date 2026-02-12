package com.luminary.ledger.service;

import com.luminary.ledger.domain.enums.AccountType;
import com.luminary.ledger.domain.exception.AccountNotFoundException;
import com.luminary.ledger.domain.exception.DuplicateAccountNameException;
import com.luminary.ledger.domain.model.Account;
import com.luminary.ledger.domain.vo.AccountName;
import com.luminary.ledger.persistence.entity.AccountEntity;
import com.luminary.ledger.persistence.mapper.AccountEntityMapper;
import com.luminary.ledger.persistence.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountEntityMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountEntityMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public Account createAccount(AccountName name, AccountType type) {
        if (accountRepository.existsByName(name.value())) {
            throw new DuplicateAccountNameException(name.value());
        }

        Account account = Account.create(name, type);
        AccountEntity saved = accountRepository.save(accountMapper.toEntity(account));
        return accountMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        AccountEntity entity = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return accountMapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Page<Account> listAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(accountMapper::toDomain);
    }
}
