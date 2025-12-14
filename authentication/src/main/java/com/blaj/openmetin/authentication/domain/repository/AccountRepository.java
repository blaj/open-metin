package com.blaj.openmetin.authentication.domain.repository;

import com.blaj.openmetin.authentication.domain.entity.Account;
import com.blaj.openmetin.shared.domain.repository.AuditingEntityRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends AuditingEntityRepository<Account> {

  Optional<Account> findByUsername(String username);
}
