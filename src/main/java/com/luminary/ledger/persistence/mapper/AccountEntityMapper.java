package com.luminary.ledger.persistence.mapper;

import com.luminary.ledger.domain.model.Account;
import com.luminary.ledger.domain.vo.AccountName;
import com.luminary.ledger.persistence.entity.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountEntityMapper {

    public Account toDomain(AccountEntity entity) {
        return new Account(
                entity.getId(),
                AccountName.of(entity.getName()),
                entity.getType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public AccountEntity toEntity(Account domain) {
        AccountEntity entity = new AccountEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName().value());
        entity.setType(domain.getType());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
