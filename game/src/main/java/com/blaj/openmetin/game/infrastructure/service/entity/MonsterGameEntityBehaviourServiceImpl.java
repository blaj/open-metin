package com.blaj.openmetin.game.infrastructure.service.entity;

import com.blaj.openmetin.game.application.common.character.dto.MoveCharacterBroadcastPacket;
import com.blaj.openmetin.game.application.common.entity.GameEntityMovementService;
import com.blaj.openmetin.game.domain.enums.character.CharacterMovementType;
import com.blaj.openmetin.game.domain.enums.entity.EntityState;
import com.blaj.openmetin.game.domain.enums.map.MapAttribute;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.entity.MonsterGameEntity;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import com.blaj.openmetin.shared.common.utils.MathUtils;
import com.blaj.openmetin.shared.domain.model.Coordinates;
import com.blaj.openmetin.shared.domain.model.Vector2;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonsterGameEntityBehaviourServiceImpl
    implements GameEntityBehaviourService<MonsterGameEntity> {

  private static final int CHECK_VALID_POSITION_MAX_ATTEMPTS = 16;
  private static final int MIN_MOVE_DISTANCE = 300;
  private static final int MAX_MOVE_DISTANCE = 700;
  private static final EnumSet<MapAttribute> BLOCK_MAP_ATTRIBUTES =
      EnumSet.of(MapAttribute.BLOCK, MapAttribute.OBJECT);

  private final GameEntityMovementService gameEntityMovementService;
  private final SessionService sessionService;

  @Override
  public void update(MonsterGameEntity monsterGameEntity) {
    if (monsterGameEntity.getState().equals(EntityState.IDLE)) {
      var currentTime = DateTimeUtils.getUnixTime();
      var nextMovementTime = monsterGameEntity.getBehaviourState().getNextMovementTime();

      if (currentTime >= nextMovementTime) {
        moveToRandomLocation(monsterGameEntity);

        var newNextMovementTime = currentTime + ThreadLocalRandom.current().nextLong(10000, 20000);
        monsterGameEntity.getBehaviourState().setNextMovementTime(newNextMovementTime);
      }
    }
  }

  @Override
  public Class<MonsterGameEntity> getSupportedEntityClass() {
    return MonsterGameEntity.class;
  }

  private void moveToRandomLocation(MonsterGameEntity monsterGameEntity) {
    for (var i = 0; i < CHECK_VALID_POSITION_MAX_ATTEMPTS; i++) {
      var distance = ThreadLocalRandom.current().nextInt(MIN_MOVE_DISTANCE, MAX_MOVE_DISTANCE);
      var directionVector =
          MathUtils.getDirectionVector(ThreadLocalRandom.current().nextDouble(0, 360));

      var delta = new Vector2(distance * directionVector.x(), distance * directionVector.y());
      var targetCoordinates = monsterGameEntity.getCoordinates().add(delta);

      if (tryGoToLocation(monsterGameEntity, targetCoordinates)) {
        return;
      }
    }
  }

  private boolean tryGoToLocation(MonsterGameEntity monsterGameEntity, Coordinates coordinates) {
    var map = monsterGameEntity.getMap();

    if (!map.isPositionInside(coordinates)) {
      return false;
    }

    if (map.hasAnyMapAttributeOnCoordinates(coordinates, BLOCK_MAP_ATTRIBUTES)) {
      return false;
    }

    if (map.hasAttributeOnStraightPath(
        monsterGameEntity.getCoordinates(), coordinates, BLOCK_MAP_ATTRIBUTES)) {
      return false;
    }

    var currentTime = DateTimeUtils.getUnixTime();

    var directionX = coordinates.x() - monsterGameEntity.getPositionX();
    var directionY = coordinates.y() - monsterGameEntity.getPositionY();
    var rotation = MathUtils.rotation(directionX, directionY);
    monsterGameEntity.setRotation((float) rotation);

    gameEntityMovementService.goTo(
        monsterGameEntity, coordinates.x(), coordinates.y(), currentTime);

    var moveCharacterBroadcastPacket =
        new MoveCharacterBroadcastPacket()
            .setMovementType(CharacterMovementType.MOVE)
            .setArgument(CharacterMovementType.WAIT.getValue())
            .setRotation((short) (monsterGameEntity.getRotation() / 5))
            .setVid(monsterGameEntity.getVid())
            .setPositionX(monsterGameEntity.getTargetPositionX())
            .setPositionY(monsterGameEntity.getTargetPositionY())
            .setTime(currentTime)
            .setDuration(monsterGameEntity.getMovementDuration());

    monsterGameEntity.getNearbyEntities().stream()
        .filter(nearbyEntity -> nearbyEntity instanceof GameCharacterEntity)
        .map(nearbyEntity -> (GameCharacterEntity) nearbyEntity)
        .map(GameCharacterEntity::getSession)
        .map(Session::getId)
        .forEach(
            sessionId -> sessionService.sendPacketAsync(sessionId, moveCharacterBroadcastPacket));

    return true;
  }
}
