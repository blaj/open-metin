package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.application.common.entity.MonsterGameEntityFactoryService;
import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointType;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.spawn.MonsterGroup;
import com.blaj.openmetin.game.domain.model.spawn.SpawnGroup;
import com.blaj.openmetin.game.domain.model.spawn.SpawnPoint;
import com.blaj.openmetin.game.infrastructure.service.world.GameWorldService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessMapSpawnPointsService {

  private final MonsterGameEntityFactoryService monsterGameEntityFactoryService;

  private final java.util.Map<SpawnPointType, SpawnPointTypeHandler> spawnPointConsumerStrategyMap;

  public ProcessMapSpawnPointsService(
      MonsterGameEntityFactoryService monsterGameEntityFactoryService) {
    this.monsterGameEntityFactoryService = monsterGameEntityFactoryService;
    this.spawnPointConsumerStrategyMap = initializeSpawnPointConsumerStrategyMap();
  }

  public void process(Map map, GameWorldService gameWorldService) {
    map.getSpawnPoints()
        .forEach(
            spawnPoint -> {
              var monsterGroup =
                  MonsterGroup.builder()
                      .monsterEntities(new ArrayList<>())
                      .spawnPoint(spawnPoint)
                      .build();

              spawnGroup(monsterGroup, map, gameWorldService);
            });
  }

  private void spawnGroup(MonsterGroup monsterGroup, Map map, GameWorldService gameWorldService) {
    var spawnPoint = monsterGroup.getSpawnPoint();

    if (spawnPoint == null) {
      return;
    }

    Optional.ofNullable(spawnPointConsumerStrategyMap.get(spawnPoint.getType()))
        .ifPresentOrElse(
            spawnPointStrategy -> {
              spawnPointStrategy.handle(monsterGroup, map, gameWorldService);
            },
            () -> log.warn("Unknown spawn point type: {}", spawnPoint.getType()));
  }

  private void processSpawnPointTypeMonster(
      MonsterGroup monsterGroup, Map map, GameWorldService gameWorldService) {
    var spawnPoint = monsterGroup.getSpawnPoint();
    var monsterGameEntity =
        monsterGameEntityFactoryService.createForSpawn(spawnPoint.getMonsterId(), spawnPoint, map);

    if (monsterGameEntity == null) {
      log.debug(
          "Failed to spawn monster {} at spawn point coordinates({}, {})",
          spawnPoint.getMonsterId(),
          spawnPoint.getX(),
          spawnPoint.getY());

      return;
    }

    spawnPoint.setCurrentGroup(monsterGroup);
    monsterGroup.getMonsterEntities().add(monsterGameEntity);
    monsterGameEntity.setMonsterGroup(monsterGroup);

    gameWorldService.spawnEntity(monsterGameEntity);

    log.debug(
        "Spawned monster {} (VID: {}) at ({}, {})",
        monsterGameEntity.getMonsterDefinition().getName(),
        monsterGameEntity.getVid(),
        monsterGameEntity.getPositionX(),
        monsterGameEntity.getPositionY());
  }

  private void processSpawnPointTypeGroup(
      MonsterGroup monsterGroup, Map map, GameWorldService gameWorldService) {
    var spawnPoint = monsterGroup.getSpawnPoint();

    gameWorldService
        .getGroup(spawnPoint.getMonsterId())
        .ifPresentOrElse(
            group -> spawnGroupInternal(monsterGroup, spawnPoint, group, map, gameWorldService),
            () -> log.warn("Group not found: {}", spawnPoint.getMonsterId()));
  }

  private void processSpawnPointTypeGroupCollection(
      MonsterGroup monsterGroup, Map map, GameWorldService gameWorldService) {
    var spawnPoint = monsterGroup.getSpawnPoint();

    var groupCollectionOptional = gameWorldService.getGroupCollection(spawnPoint.getMonsterId());
    if (groupCollectionOptional.isEmpty()) {
      log.warn("Group collection not found: {}", spawnPoint.getMonsterId());
      return;
    }

    var groupCollection = groupCollectionOptional.get();

    var entries = groupCollection.entries();
    if (entries.isEmpty()) {
      log.warn("Group collection {} has no entries", groupCollection.id());
      return;
    }

    var index = ThreadLocalRandom.current().nextInt(entries.size());
    var collectionEntry = entries.get(index);

    var groupOptional = gameWorldService.getGroup(collectionEntry.id());
    if (groupOptional.isEmpty()) {
      log.warn("Group not found in collection: {}", collectionEntry.id());
      return;
    }

    var group = groupOptional.get();
    var probability = collectionEntry.probability();

    if (shouldSpawnBasedOnProbability(probability)) {
      spawnGroupInternal(monsterGroup, spawnPoint, group, map, gameWorldService);
    } else {
      log.debug("Group {} skipped due to probability {}", group.id(), probability);
    }
  }

  private void spawnGroupInternal(
      MonsterGroup monsterGroup,
      SpawnPoint spawnPoint,
      SpawnGroup group,
      Map map,
      GameWorldService gameWorldService) {
    spawnPoint.setCurrentGroup(monsterGroup);

    var leaderMonsterGameEntity =
        monsterGameEntityFactoryService.createForSpawn(group.leaderId(), spawnPoint, map);
    if (leaderMonsterGameEntity == null) {
      log.warn(
          "Failed to spawn group leader {} at spawn point ({}, {})",
          group.leaderId(),
          spawnPoint.getX(),
          spawnPoint.getY());
      return;
    }

    monsterGroup.getMonsterEntities().add(leaderMonsterGameEntity);
    leaderMonsterGameEntity.setMonsterGroup(monsterGroup);

    gameWorldService.spawnEntity(leaderMonsterGameEntity);

    log.debug(
        "Spawned group leader {} (VID: {})",
        leaderMonsterGameEntity.getEntityClass(),
        leaderMonsterGameEntity.getVid());

    for (var memberId : group.membersIds()) {
      var memberMonsterGameEntity =
          monsterGameEntityFactoryService.createForSpawn(memberId, spawnPoint, map);

      if (memberMonsterGameEntity == null) {
        log.debug("Failed to spawn group member {} (continuing with next)", memberId);
        continue;
      }

      monsterGroup.getMonsterEntities().add(memberMonsterGameEntity);
      memberMonsterGameEntity.setMonsterGroup(monsterGroup);

      gameWorldService.spawnEntity(memberMonsterGameEntity);

      log.debug(
          "Spawned group member {} (VID: {})",
          memberMonsterGameEntity.getEntityClass(),
          memberMonsterGameEntity.getVid());
    }

    log.info(
        "Spawned group {} with {} monsters (leader + {} members)",
        group.id(),
        monsterGroup.getMonsterEntities().size(),
        group.membersIds().size());
  }

  private boolean shouldSpawnBasedOnProbability(float probability) {
    if (probability >= 1.0f) {
      return true;
    }

    return ThreadLocalRandom.current().nextFloat() <= probability;
  }

  private java.util.Map<SpawnPointType, SpawnPointTypeHandler>
      initializeSpawnPointConsumerStrategyMap() {
    var map = new EnumMap<SpawnPointType, SpawnPointTypeHandler>(SpawnPointType.class);

    map.put(SpawnPointType.MONSTER, this::processSpawnPointTypeMonster);
    map.put(SpawnPointType.GROUP, this::processSpawnPointTypeGroup);
    map.put(SpawnPointType.GROUP_COLLECTION, this::processSpawnPointTypeGroupCollection);

    return map;
  }

  @FunctionalInterface
  private interface SpawnPointTypeHandler {

    void handle(MonsterGroup monsterGroup, Map map, GameWorldService gameWorldService);
  }
}
