package com.blaj.openmetin.game.domain.model;

import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.domain.enums.EntityState;
import com.blaj.openmetin.game.domain.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public abstract class BaseGameEntity {

  private long vid;
  private EntityState state;
  private int positionX;
  private int positionY;
  private Empire empire;
  private short movementSpeed;
  private short attackSpeed;
  private long health;
  private long mana;

  public abstract EntityType getType();
}
