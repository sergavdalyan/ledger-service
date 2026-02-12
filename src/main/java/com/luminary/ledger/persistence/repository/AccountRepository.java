package com.luminary.ledger.persistence.repository;

import com.luminary.ledger.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    boolean existsByName(String name);
}
