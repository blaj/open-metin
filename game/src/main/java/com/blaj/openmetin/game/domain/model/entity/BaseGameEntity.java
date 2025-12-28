package com.blaj.openmetin.game.domain.model.entity;

import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.domain.enums.entity.EntityState;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
  private long entityClass;
  private float rotation;
  private int positionX;
  private int positionY;
  private int startPositionX;
  private int startPositionY;
  private int targetPositionX;
  private int targetPositionY;
  private long movementStartAt;
  private long movementDuration;
  private boolean positionChanged;
  private Empire empire;
  private short movementSpeed;
  private short attackSpeed;
  private long health;
  private long mana;

  private final Set<BaseGameEntity> nearbyEntities = ConcurrentHashMap.newKeySet();

  public abstract EntityType getType();

  public void addNearbyEntity(BaseGameEntity entity) {
    nearbyEntities.add(entity);
  }

  public void removeNearbyEntity(BaseGameEntity entity) {
    nearbyEntities.remove(entity);
  }
}
