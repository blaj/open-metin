package com.blaj.openmetin.game.domain.model.entity;

import com.blaj.openmetin.game.domain.entity.MonsterDefinition;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.spawn.MonsterGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MonsterGameEntity extends BaseGameEntity {

  private MonsterDefinition monsterDefinition;
  private MonsterGroup monsterGroup;
  private BehaviourState behaviourState;

  @Override
  public EntityType getType() {
    return EntityType.MONSTER;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BehaviourState {
    private Long nextMovementTime;
  }
}
