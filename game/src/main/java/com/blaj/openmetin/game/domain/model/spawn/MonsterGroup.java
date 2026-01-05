package com.blaj.openmetin.game.domain.model.spawn;

import com.blaj.openmetin.game.domain.model.entity.MonsterGameEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MonsterGroup {

  @Builder.Default private List<MonsterGameEntity> monsterEntities = new ArrayList<>();

  private SpawnPoint spawnPoint;
}
