package com.blaj.openmetin.game.infrastructure.service.tick;

import com.blaj.openmetin.game.application.common.entity.EntityVisibilityService;
import com.blaj.openmetin.game.domain.enums.entity.EntityState;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.spatial.QuadTree;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntityUpdateService {

  private static final int VIEW_DISTANCE = 10000;

  private final ThreadLocal<List<BaseGameEntity>> nearbyCache =
      ThreadLocal.withInitial(ArrayList::new);
  private final ThreadLocal<Set<BaseGameEntity>> removeCache =
      ThreadLocal.withInitial(HashSet::new);

  private final EntityVisibilityService entityVisibilityService;

  public void update(Map map) {
    var entities = map.getEntities();
    var quadTree = map.getQuadTree();
    var currentServerTime = DateTimeUtils.getUnixTime();

    for (var entity : entities) {
      updateEntityMovement(entity, currentServerTime);
      updatePositionInQuadTree(entity, quadTree);
    }
  }

  private void updateEntityMovement(BaseGameEntity gameEntity, long currentServerTime) {
    if (gameEntity.getState() != EntityState.MOVING) {
      return;
    }

    var elapsed = currentServerTime - gameEntity.getMovementStartAt();
    var rate = calculateMovementRate(gameEntity, elapsed);

    var newX =
        interpolatePosition(gameEntity.getStartPositionX(), gameEntity.getTargetPositionX(), rate);
    var newY =
        interpolatePosition(gameEntity.getStartPositionY(), gameEntity.getTargetPositionY(), rate);

    gameEntity.setPositionX(newX);
    gameEntity.setPositionY(newY);

    if (rate >= 1) {
      gameEntity.setState(EntityState.IDLE);
    }
  }

  private void updatePositionInQuadTree(BaseGameEntity gameEntity, QuadTree quadTree) {
    if (!gameEntity.isPositionChanged()) {
      return;
    }

    gameEntity.setPositionChanged(false);
    quadTree.updatePosition(gameEntity);

    if (gameEntity.getType() != EntityType.PLAYER) {
      return;
    }

    var nearby = nearbyCache.get();
    var remove = removeCache.get();

    var entityTypeFilter = gameEntity.getType() != EntityType.PLAYER ? EntityType.PLAYER : null;

    quadTree.queryAround(
        nearby,
        gameEntity.getPositionX(),
        gameEntity.getPositionY(),
        VIEW_DISTANCE,
        entityTypeFilter);

    gameEntity.getNearbyEntities().stream()
        .filter(nearbyEntity -> !nearby.remove(nearbyEntity))
        .forEach(remove::add);

    remove.forEach(
        removeEntity -> {
          removeEntity.removeNearbyEntity(gameEntity);
          gameEntity.removeNearbyEntity(removeEntity);

          var playerEntity = (GameCharacterEntity) gameEntity;
          entityVisibilityService.hideEntityFromPlayer(
              removeEntity, playerEntity.getSession().getId());

          if (removeEntity.getType() == EntityType.PLAYER) {
            var nearbyPlayerEntity = (GameCharacterEntity) removeEntity;

            entityVisibilityService.hideEntityFromPlayer(
                gameEntity, nearbyPlayerEntity.getSession().getId());
          }
        });

    nearby.stream()
        .filter(nearbyEntity -> nearbyEntity != gameEntity)
        .forEach(
            nearbyEntity -> {
              nearbyEntity.addNearbyEntity(gameEntity);
              gameEntity.addNearbyEntity(nearbyEntity);

              var playerEntity = (GameCharacterEntity) gameEntity;
              entityVisibilityService.showEntityToPlayer(
                  nearbyEntity, playerEntity.getSession().getId());

              if (nearbyEntity.getType() == EntityType.PLAYER) {
                var nearbyPlayerEntity = (GameCharacterEntity) nearbyEntity;

                entityVisibilityService.showEntityToPlayer(
                    gameEntity, nearbyPlayerEntity.getSession().getId());
              }
            });

    nearby.clear();
    remove.clear();
  }

  private float calculateMovementRate(BaseGameEntity gameEntity, long elapsed) {
    if (gameEntity.getMovementDuration() == 0) {
      return 1.0f;
    }

    var rate = (float) elapsed / gameEntity.getMovementDuration();
    return Math.min(rate, 1.0f);
  }

  private int interpolatePosition(int start, int target, float rate) {
    return (int) ((target - start) * rate + start);
  }
}
