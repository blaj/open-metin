package com.blaj.openmetin.game.infrastructure.service.tick;

import com.blaj.openmetin.game.application.common.entity.EntityVisibilityService;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.spatial.QuadTree;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntityDespawnService {

  private final EntityVisibilityService entityVisibilityService;

  public void processPendingRemovals(Map map) {
    var pendingRemovals = map.getPendingRemovals();
    var entities = map.getEntities();
    var quadTree = map.getQuadTree();

    BaseGameEntity gameEntity;
    while ((gameEntity = pendingRemovals.poll()) != null) {
      despawnEntity(gameEntity, entities, quadTree);
    }
  }

  private void despawnEntity(
      BaseGameEntity gameEntity, List<BaseGameEntity> entities, QuadTree quadTree) {
    entities.remove(gameEntity);

    gameEntity
        .getNearbyEntities()
        .forEach(
            nearbyEntity -> {
              nearbyEntity.removeNearbyEntity(gameEntity);
              asPlayer(nearbyEntity)
                  .ifPresent(
                      player ->
                          entityVisibilityService.hideEntityFromPlayer(
                              gameEntity, player.getSession().getId()));
            });

    gameEntity.getNearbyEntities().clear();
    gameEntity.setMap(null);

    quadTree.remove(gameEntity);
  }

  private Optional<GameCharacterEntity> asPlayer(BaseGameEntity gameEntity) {
    return gameEntity.getType() == EntityType.PLAYER
        ? Optional.of((GameCharacterEntity) gameEntity)
        : Optional.empty();
  }
}
