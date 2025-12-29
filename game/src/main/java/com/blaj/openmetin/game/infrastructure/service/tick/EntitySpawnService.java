package com.blaj.openmetin.game.infrastructure.service.tick;

import com.blaj.openmetin.game.application.common.entity.EntityVisibilityService;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntitySpawnService {

  private static final int VIEW_DISTANCE = 10000;

  private final EntityVisibilityService entityVisibilityService;

  public void processPendingSpawns(Map map) {
    var pendingSpawns = map.getPendingSpawns();
    var quadTree = map.getQuadTree();
    var entities = map.getEntities();

    BaseGameEntity entity;
    while ((entity = pendingSpawns.poll()) != null) {
      spawnEntity(entity, entities, quadTree, map);
    }
  }

  private void spawnEntity(
      BaseGameEntity gameEntity,
      java.util.List<BaseGameEntity> entities,
      com.blaj.openmetin.game.domain.model.spatial.QuadTree quadTree,
      Map map) {
    if (!quadTree.insert(gameEntity)) {
      return;
    }

    var aroundEntities = new ArrayList<BaseGameEntity>();
    var entityTypeFilter = gameEntity.getType() != EntityType.PLAYER ? EntityType.PLAYER : null;

    quadTree.queryAround(
        aroundEntities,
        gameEntity.getPositionX(),
        gameEntity.getPositionY(),
        VIEW_DISTANCE,
        entityTypeFilter);

    aroundEntities.stream()
        .filter(nearbyEntity -> nearbyEntity != gameEntity)
        .forEach(nearbyEntity -> handleNearbyEntity(gameEntity, nearbyEntity));

    entities.add(gameEntity);
    gameEntity.setMap(map);
  }

  private void handleNearbyEntity(BaseGameEntity gameEntity, BaseGameEntity nearbyEntity) {
    gameEntity.addNearbyEntity(nearbyEntity);
    nearbyEntity.addNearbyEntity(gameEntity);

    asPlayer(gameEntity)
        .ifPresent(
            player ->
                entityVisibilityService.showEntityToPlayer(
                    nearbyEntity, player.getSession().getId()));

    asPlayer(nearbyEntity)
        .ifPresent(
            player ->
                entityVisibilityService.showEntityToPlayer(
                    gameEntity, player.getSession().getId()));
  }

  private Optional<GameCharacterEntity> asPlayer(BaseGameEntity gameEntity) {
    return gameEntity.getType() == EntityType.PLAYER
        ? Optional.of((GameCharacterEntity) gameEntity)
        : Optional.empty();
  }
}
