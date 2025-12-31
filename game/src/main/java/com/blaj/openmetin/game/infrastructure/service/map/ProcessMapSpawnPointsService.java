package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.application.common.entity.MonsterGameEntityFactoryService;
import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointType;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.spawn.MonsterGroup;
import com.blaj.openmetin.game.infrastructure.service.world.GameWorldService;
import java.util.EnumMap;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessMapSpawnPointsService {

  private final MonsterGameEntityFactoryService monsterGameEntityFactoryService;

  private final java.util.Map<SpawnPointType, BiConsumer<MonsterGroup, Map>>
      spawnPointConsumerStrategyMap;

  public ProcessMapSpawnPointsService(
      MonsterGameEntityFactoryService monsterGameEntityFactoryService) {
    this.monsterGameEntityFactoryService = monsterGameEntityFactoryService;
    this.spawnPointConsumerStrategyMap = initializeSpawnPointConsumerStrategyMap();
  }

  public void process(Map map, GameWorldService gameWorldService) {
    map.getSpawnPoints()
        .forEach(
            spawnPoint -> {
              var monsterGroup = MonsterGroup.builder().spawnPoint(spawnPoint).build();

              spawnGroup(monsterGroup, map);
            });
  }

  private void spawnGroup(MonsterGroup monsterGroup, Map map) {
    var spawnPoint = monsterGroup.getSpawnPoint();

    if (spawnPoint == null) {
      return;
    }

    Optional.ofNullable(spawnPointConsumerStrategyMap.get(spawnPoint.getType()))
        .ifPresentOrElse(
            spawnPointStrategy -> {
              spawnPointStrategy.accept(monsterGroup, map);
            },
            () -> log.warn("Unknown spawn point type: {}", spawnPoint.getType()));
  }

  private void processSpawnPointTypeMonster(MonsterGroup monsterGroup, Map map) {
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

    log.debug(
        "Spawned monster {} (VID: {}) at ({}, {})",
        monsterGameEntity.getMonsterDefinition().getName(),
        monsterGameEntity.getVid(),
        monsterGameEntity.getPositionX(),
        monsterGameEntity.getPositionY());
  }

  private void processSpawnPointTypeGroup(MonsterGroup monsterGroup, Map map) {}

  private void processSpawnPointTypeGroupCollection(MonsterGroup monsterGroup, Map map) {}

  private java.util.Map<SpawnPointType, BiConsumer<MonsterGroup, Map>>
      initializeSpawnPointConsumerStrategyMap() {
    var map = new EnumMap<SpawnPointType, BiConsumer<MonsterGroup, Map>>(SpawnPointType.class);

    map.put(SpawnPointType.MONSTER, this::processSpawnPointTypeMonster);
    map.put(SpawnPointType.GROUP, this::processSpawnPointTypeGroup);
    map.put(SpawnPointType.GROUP_COLLECTION, this::processSpawnPointTypeGroupCollection);

    return map;
  }
}
