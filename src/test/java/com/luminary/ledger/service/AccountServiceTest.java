package com.luminary.ledger.service;

import com.luminary.ledger.domain.enums.AccountType;
import com.luminary.ledger.domain.exception.AccountNotFoundException;
import com.luminary.ledger.domain.exception.DuplicateAccountNameException;
import com.luminary.ledger.domain.model.Account;
import com.luminary.ledger.domain.vo.AccountName;
import com.luminary.ledger.persistence.entity.AccountEntity;
import com.luminary.ledger.persistence.mapper.AccountEntityMapper;
import com.luminary.ledger.persistence.repository.AccountRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountEntityMapper accountMapper;
    private AccountService accountService;

    private AccountEntity accountEntity;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountEntityMapper();
        accountService = new AccountService(accountRepository, accountMapper);

        LocalDateTime now = LocalDateTime.now();
        accountEntity = new AccountEntity();
        accountEntity.setId(1L);
        accountEntity.setName("Cash");
        accountEntity.setType(AccountType.ASSET);
        accountEntity.setCreatedAt(now);
        accountEntity.setUpdatedAt(now);
    }

    @Test
    void createAccount_success() {
        when(accountRepository.existsByName("Cash")).thenReturn(false);
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(accountEntity);

        Account result = accountService.createAccount(AccountName.of("Cash"), AccountType.ASSET);

        assertEquals(1L, result.getId());
        assertEquals("Cash", result.getName().value());
        assertEquals(AccountType.ASSET, result.getType());

        verify(accountRepository).existsByName("Cash");
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void createAccount_duplicateName_throwsException() {
        when(accountRepository.existsByName("Cash")).thenReturn(true);

        assertThrows(DuplicateAccountNameException.class,
                () -> accountService.createAccount(AccountName.of("Cash"), AccountType.ASSET));

        verify(accountRepository, never()).save(any());
    }


    @Test
    void getAccount_found() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity));

        Account result = accountService.getAccount(1L);

        assertEquals(1L, result.getId());
        assertEquals("Cash", result.getName().value());
        assertEquals(AccountType.ASSET, result.getType());
    }

    @Test
    void getAccount_notFound_throwsException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccount(99L));
    }


    @Test
    void listAccounts_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<AccountEntity> entityPage = new PageImpl<>(List.of(accountEntity), pageable, 1);
        when(accountRepository.findAll(pageable)).thenReturn(entityPage);

        Page<Account> result = accountService.listAccounts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Cash", result.getContent().get(0).getName().value());
    }

    @Test
    void listAccounts_empty() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<AccountEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(accountRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<Account> result = accountService.listAccounts(pageable);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
