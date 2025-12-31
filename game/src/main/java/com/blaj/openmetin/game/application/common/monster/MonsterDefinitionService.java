package com.blaj.openmetin.game.application.common.monster;

import com.blaj.openmetin.game.domain.entity.MonsterDefinition;
import com.blaj.openmetin.game.domain.repository.MonsterDefinitionRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonsterDefinitionService {

  private final MonsterDefinitionRepository monsterDefinitionRepository;

  @Cacheable(value = "monster_definitions")
  public List<MonsterDefinition> getMonsterDefinitions() {
    return monsterDefinitionRepository.findAll();
  }

  public Optional<MonsterDefinition> getMonsterDefinition(long id) {
    return monsterDefinitionRepository.findById(id);
  }
}
