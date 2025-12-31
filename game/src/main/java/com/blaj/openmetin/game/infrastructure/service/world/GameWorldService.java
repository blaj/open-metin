package com.blaj.openmetin.game.infrastructure.service.world;

import com.blaj.openmetin.game.application.common.game.GameWorldSpawnEntityService;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.spatial.Grid;
import com.blaj.openmetin.game.domain.model.spawn.SpawnGroup;
import com.blaj.openmetin.game.domain.model.spawn.SpawnGroupCollection;
import com.blaj.openmetin.game.infrastructure.service.map.AtlasMapProviderService;
import com.blaj.openmetin.game.infrastructure.service.map.GroupCollectionFileLoaderService;
import com.blaj.openmetin.game.infrastructure.service.map.GroupFileLoaderService;
import com.blaj.openmetin.game.infrastructure.service.map.MapAttributeProviderService;
import com.blaj.openmetin.game.infrastructure.service.map.ProcessMapSpawnPointsService;
import com.blaj.openmetin.game.infrastructure.service.map.SpawnPointFileLoaderService;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameWorldService implements GameWorldSpawnEntityService {

  private final AtlasMapProviderService atlasMapProviderService;
  private final MapAttributeProviderService attributeProviderService;
  private final SpawnPointFileLoaderService spawnPointFileLoaderService;
  private final GroupFileLoaderService groupFileLoaderService;
  private final GroupCollectionFileLoaderService groupCollectionFileLoaderService;
  private final ProcessMapSpawnPointsService processMapSpawnPointsService;

  private final java.util.Map<Long, SpawnGroup> spawnGroups = new ConcurrentHashMap<>();
  private final java.util.Map<Long, SpawnGroupCollection> spawnGroupCollections =
      new ConcurrentHashMap<>();
  private final java.util.Map<String, GameCharacterEntity> gameCharacterEntities =
      new ConcurrentHashMap<>();
  private final Grid<Map> mapDataGrid = new Grid<>(Map.class, 0, 0);
  @Getter private final java.util.Map<String, Map> maps = new ConcurrentHashMap<>();

  public void loadMaps() {
    spawnGroups.putAll(groupFileLoaderService.load());
    spawnGroupCollections.putAll(groupCollectionFileLoaderService.load());

    var allMaps = atlasMapProviderService.getAll();

    if (allMaps.isEmpty()) {
      log.warn("No maps found, skipping map grid initialization");
      return;
    }

    allMaps.forEach(
        map ->
            attributeProviderService
                .getAttributes(map.getName(), map.getCoordinates(), map.getWidth(), map.getHeight())
                .ifPresent(map::setMapAttributeSet));

    allMaps.forEach(
        map -> map.getSpawnPoints().addAll(spawnPointFileLoaderService.load(map.getName())));

    allMaps.forEach(map -> maps.put(map.getName(), map));

    var maxX =
        allMaps.stream()
            .mapToInt(map -> map.getCoordinates().x() + map.getWidth() * Map.MAP_UNIT)
            .max()
            .orElse(0);
    var maxY =
        allMaps.stream()
            .mapToInt(map -> map.getCoordinates().y() + map.getHeight() * Map.MAP_UNIT)
            .max()
            .orElse(0);

    mapDataGrid.resize(maxX / Map.MAP_UNIT, maxY / Map.MAP_UNIT);

    maps.forEach(
        (mapName, map) -> {
          for (var x = map.getUnitX(); x < map.getUnitX() + map.getWidth(); x++) {
            for (var y = map.getUnitY(); y < map.getUnitY() + map.getHeight(); y++) {
              mapDataGrid.set(x, y, map);
            }
          }
        });

    maps.forEach(
        (mapName, map) -> {
          processMapSpawnPointsService.process(map, this);
        });

    var test = 123;
  }

  public Optional<Map> getMap(int x, int y) {
    var gridX = x / Map.MAP_UNIT;
    var gridY = y / Map.MAP_UNIT;

    return mapDataGrid.get(gridX, gridY);
  }

  public void spawnEntity(BaseGameEntity baseGameEntity) {
    var map = getMap(baseGameEntity.getPositionX(), baseGameEntity.getPositionY()).orElse(null);

    if (map == null) {
      return;
    }

    if (baseGameEntity instanceof GameCharacterEntity) {
      addGameCharacterEntity((GameCharacterEntity) baseGameEntity);
    }

    map.getPendingSpawns().add(baseGameEntity);
  }

  public Optional<SpawnGroup> getGroup(long groupId) {
    return Optional.ofNullable(spawnGroups.get(groupId));
  }

  public Optional<SpawnGroupCollection> getGroupCollection(long collectionId) {
    return Optional.ofNullable(spawnGroupCollections.get(collectionId));
  }

  private void addGameCharacterEntity(GameCharacterEntity gameCharacterEntity) {
    var name = gameCharacterEntity.getCharacterDto().getName();

    if (gameCharacterEntities.containsKey(name)) {
      gameCharacterEntities.replace(name, gameCharacterEntity);
    } else {
      gameCharacterEntities.put(name, gameCharacterEntity);
    }
  }
}
