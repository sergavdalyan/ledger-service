package com.luminary.ledger.api.controller;

import com.luminary.ledger.api.dto.AccountResponse;
import com.luminary.ledger.api.dto.CreateAccountRequest;
import com.luminary.ledger.api.dto.TransactionResponse;
import com.luminary.ledger.api.mapper.AccountDtoMapper;
import com.luminary.ledger.api.mapper.TransactionDtoMapper;
import com.luminary.ledger.domain.enums.AccountType;
import com.luminary.ledger.domain.model.Account;
import com.luminary.ledger.domain.vo.AccountName;
import com.luminary.ledger.service.AccountService;
import com.luminary.ledger.service.BalanceCalculator;
import com.luminary.ledger.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final BalanceCalculator balanceCalculator;
    private final TransactionService transactionService;
    private final AccountDtoMapper accountDtoMapper;
    private final TransactionDtoMapper transactionDtoMapper;

    public AccountController(AccountService accountService,
                             BalanceCalculator balanceCalculator,
                             TransactionService transactionService,
                             AccountDtoMapper accountDtoMapper,
                             TransactionDtoMapper transactionDtoMapper) {
        this.accountService = accountService;
        this.balanceCalculator = balanceCalculator;
        this.transactionService = transactionService;
        this.accountDtoMapper = accountDtoMapper;
        this.transactionDtoMapper = transactionDtoMapper;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountType type;
        try {
            type = AccountType.valueOf(request.type().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid account type: " + request.type());
        }
        Account account = accountService.createAccount(AccountName.of(request.name()), type);
        BigDecimal balance = balanceCalculator.calculateBalance(account.getId(), account.getType());
        return ResponseEntity.status(HttpStatus.CREATED).body(accountDtoMapper.toResponse(account, balance));
    }

    @GetMapping
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(@PageableDefault(size = 20) Pageable pageable) {
        Page<AccountResponse> page = accountService.listAccounts(pageable)
                .map(account -> {
                    BigDecimal balance = balanceCalculator.calculateBalance(account.getId(), account.getType());
                    return accountDtoMapper.toResponse(account, balance);
                });
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long id) {
        Account account = accountService.getAccount(id);
        BigDecimal balance = balanceCalculator.calculateBalance(id, account.getType());
        return ResponseEntity.ok(accountDtoMapper.toResponse(account, balance));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getAccountTransactions(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(id, pageable)
                .map(transactionDtoMapper::toResponse);
        return ResponseEntity.ok(transactions);
    }
}
