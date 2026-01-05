package com.blaj.openmetin.game.domain.model.entity;

import com.blaj.openmetin.game.domain.entity.MonsterDefinition;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.spawn.MonsterGroup;
import lombok.AllArgsConstructor;
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

  @Override
  public EntityType getType() {
    return EntityType.MONSTER;
  }
}
