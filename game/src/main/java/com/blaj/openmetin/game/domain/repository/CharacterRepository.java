package com.blaj.openmetin.game.domain.repository;

import com.blaj.openmetin.game.domain.entity.Character;
import com.blaj.openmetin.shared.domain.repository.AuditingEntityRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends AuditingEntityRepository<Character> {
  List<Character> findAllByAccountId(long accountId);

  Optional<Character> findByAccountIdAndSlot(long accountId, int slot);

  boolean existsByName(String name);

  boolean existsBySlotAndAccountId(int slot, long accountId);

  int countByAccountId(long accountId);
}
