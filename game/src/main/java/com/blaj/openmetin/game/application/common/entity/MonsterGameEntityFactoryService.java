package com.blaj.openmetin.game.application.common.entity;

import com.blaj.openmetin.game.application.common.game.GameEntityVidAllocator;
import com.blaj.openmetin.game.application.common.monster.MonsterDefinitionService;
import com.blaj.openmetin.game.domain.enums.entity.AiFlag;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.enums.map.MapAttribute;
import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointDirection;
import com.blaj.openmetin.game.domain.model.entity.MonsterGameEntity;
import com.blaj.openmetin.game.domain.model.map.Coordinates;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.spawn.SpawnPoint;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonsterGameEntityFactoryService {

  private static final int MAX_SPAWN_ATTEMPTS = 16;
  private static final EnumSet<MapAttribute> SPAWN_BLOCKING_MAP_ATTRIBUTES =
      EnumSet.of(MapAttribute.BLOCK, MapAttribute.OBJECT, MapAttribute.SAFE);

  private final GameEntityVidAllocator gameEntityVidAllocator;
  private final MonsterDefinitionService monsterDefinitionService;

  public MonsterGameEntity create(long id) {
    var monsterDefinition = monsterDefinitionService.getMonsterDefinition(id).orElse(null);

    if (monsterDefinition == null) {
      return null;
    }

    return MonsterGameEntity.builder()
        .vid(gameEntityVidAllocator.allocate())
        .entityClass(id)
        .monsterDefinition(monsterDefinition)
        .health(monsterDefinition.getHealth())
        .movementSpeed((short) monsterDefinition.getMovementSpeed())
        .build();
  }

  public MonsterGameEntity createForSpawn(long id, SpawnPoint spawnPoint, Map map) {
    var monsterGameEntity = create(id);

    if (monsterGameEntity == null) {
      return null;
    }

    var monsterDefinition = monsterGameEntity.getMonsterDefinition();

    var ignoreAttributeCheck = shouldIgnoreAttributeCheck(monsterGameEntity);
    var foundValidPositionAttr = ignoreAttributeCheck;

    for (var attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
      int baseX = randomizeWithinRange(spawnPoint.getX(), spawnPoint.getRangeX());
      int baseY = randomizeWithinRange(spawnPoint.getY(), spawnPoint.getRangeY());

      if (!monsterDefinition.getAiFlags().contains(AiFlag.NO_MOVE)) {
        baseX = randomizeWithinRange(baseX, Map.SPAWN_BASE_OFFSET);
        baseY = randomizeWithinRange(baseY, Map.SPAWN_BASE_OFFSET);
      }

      monsterGameEntity.setPositionX(
          map.getCoordinates().x() + baseX * Map.SPAWN_POSITION_MULTIPLIER);
      monsterGameEntity.setPositionY(
          map.getCoordinates().y() + baseY * Map.SPAWN_POSITION_MULTIPLIER);

      if (ignoreAttributeCheck
          || !map.hasAnyMapAttributeOnCoordinates(
              new Coordinates(monsterGameEntity.getPositionX(), monsterGameEntity.getPositionY()),
              SPAWN_BLOCKING_MAP_ATTRIBUTES)) {
        foundValidPositionAttr = true;
        break;
      }
    }

    if (!foundValidPositionAttr) {
      return null;
    }

    if (monsterDefinition.getAiFlags().contains(AiFlag.NO_MOVE)) {
      var compassDirection = spawnPoint.getDirection().ordinal();

      if (compassDirection >= SpawnPointDirection.values().length) {
        compassDirection = SpawnPointDirection.RANDOM.ordinal();
      }

      monsterGameEntity.setRotation(Map.SPAWN_ROTATION_SLICE_DEGREES * compassDirection);
    }

    if (monsterGameEntity.getRotation() == 0) {
      monsterGameEntity.setRotation(ThreadLocalRandom.current().nextInt(0, 360));
    }

    return monsterGameEntity;
  }

  private boolean shouldIgnoreAttributeCheck(MonsterGameEntity monster) {
    var type = monster.getType();

    return type == EntityType.NPC || type == EntityType.WARP || type == EntityType.GOTO;
  }

  private int randomizeWithinRange(int value, int range) {
    if (range == 0) {
      return value;
    }
    return value + ThreadLocalRandom.current().nextInt(-range, range + 1);
  }
}
