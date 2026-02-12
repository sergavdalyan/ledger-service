package com.luminary.ledger.api.controller;

import com.luminary.ledger.api.dto.CreateTransactionRequest;
import com.luminary.ledger.api.dto.TransactionEntryRequest;
import com.luminary.ledger.api.dto.TransactionResponse;
import com.luminary.ledger.api.mapper.TransactionDtoMapper;
import com.luminary.ledger.domain.enums.EntryType;
import com.luminary.ledger.domain.model.Transaction;
import com.luminary.ledger.domain.model.TransactionEntry;
import com.luminary.ledger.domain.vo.Money;
import com.luminary.ledger.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionDtoMapper transactionDtoMapper;

    public TransactionController(TransactionService transactionService,
                                 TransactionDtoMapper transactionDtoMapper) {
        this.transactionService = transactionService;
        this.transactionDtoMapper = transactionDtoMapper;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction( //Must validate that debits equal credits!
            @Valid @RequestBody CreateTransactionRequest request) {

        List<TransactionEntry> entries = request.entries().stream()
                .map(this::toTransactionEntry)
                .toList();

        Transaction transaction = Transaction.create(request.description(), request.date(), entries);
        Transaction saved = transactionService.createTransaction(transaction);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionDtoMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id) {
        Transaction transaction = transactionService.getTransaction(id);
        return ResponseEntity.ok(transactionDtoMapper.toResponse(transaction));
    }

    private TransactionEntry toTransactionEntry(TransactionEntryRequest request) {
        EntryType entryType = EntryType.valueOf(request.type().toUpperCase());
        return TransactionEntry.create(request.accountId(), entryType, Money.of(request.amount()));
    }
}
