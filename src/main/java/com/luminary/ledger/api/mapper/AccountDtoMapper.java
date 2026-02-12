package com.luminary.ledger.api.mapper;

import com.luminary.ledger.api.dto.AccountResponse;
import com.luminary.ledger.domain.model.Account;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountDtoMapper {

    public AccountResponse toResponse(Account account, BigDecimal balance) {
        return new AccountResponse(
                account.getId(),
                account.getName().value(),
                account.getType().name(),
                balance,
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
