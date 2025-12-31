package com.blaj.openmetin.game.domain.model.spawn;

import com.blaj.openmetin.game.domain.model.entity.MonsterGameEntity;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

@Builder
@Getter
@Setter
public class MonsterGroup {

  @Singular private List<MonsterGameEntity> monsterEntities;

  private SpawnPoint spawnPoint;
}
