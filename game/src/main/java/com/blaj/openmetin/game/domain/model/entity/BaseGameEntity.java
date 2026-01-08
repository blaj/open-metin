package com.blaj.openmetin.game.domain.model.entity;

import com.blaj.openmetin.game.domain.enums.character.Empire;
import com.blaj.openmetin.game.domain.enums.entity.EntityState;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.spatial.QuadTree;
import com.blaj.openmetin.shared.domain.model.Coordinates;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseGameEntity {
  @EqualsAndHashCode.Include private long vid;

  private EntityState state;
  private long entityClass;
  private float rotation;

  @Setter(AccessLevel.NONE)
  private int positionX;

  @Setter(AccessLevel.NONE)
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
  private QuadTree lastQuadTree;
  private Map map;
  private int lastPositionX;
  private int lastPositionY;

  private final Set<BaseGameEntity> nearbyEntities = ConcurrentHashMap.newKeySet();

  public abstract EntityType getType();

  public void setPositionX(int positionX) {
    this.positionChanged = this.positionChanged || this.positionX != positionX;
    this.positionX = positionX;
  }

  public void setPositionY(int positionY) {
    this.positionChanged = this.positionChanged || this.positionY != positionY;
    this.positionY = positionY;
  }

  public void addNearbyEntity(BaseGameEntity entity) {
    nearbyEntities.add(entity);
  }

  public void removeNearbyEntity(BaseGameEntity entity) {
    nearbyEntities.remove(entity);
  }

  public Coordinates getCoordinates() {
    return new Coordinates(positionX, positionY);
  }
}
