package com.blaj.openmetin.game.domain.model;

import com.blaj.openmetin.game.domain.enums.EntityType;
import com.blaj.openmetin.shared.common.model.Session;
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
public class GameCharacterEntity extends BaseGameEntity {

  private Session session;
  private CharacterDto characterDto;
  private long defence;

  @Override
  public EntityType getType() {
    return EntityType.PLAYER;
  }
}
