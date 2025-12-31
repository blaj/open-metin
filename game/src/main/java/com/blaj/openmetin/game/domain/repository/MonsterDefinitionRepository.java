package com.blaj.openmetin.game.domain.repository;

import com.blaj.openmetin.game.domain.entity.MonsterDefinition;
import com.blaj.openmetin.shared.domain.repository.ArchiveEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonsterDefinitionRepository extends ArchiveEntityRepository<MonsterDefinition> {}
