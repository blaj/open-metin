package com.blaj.openmetin.game.domain.repository;

import com.blaj.openmetin.game.domain.entity.Character;
import com.blaj.openmetin.shared.domain.repository.AuditingEntityRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends AuditingEntityRepository<Character> {
  List<Character> findAllByAccountId(long accountId);
}
