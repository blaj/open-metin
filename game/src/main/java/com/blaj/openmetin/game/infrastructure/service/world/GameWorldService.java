package com.blaj.openmetin.game.infrastructure.service.world;

import com.blaj.openmetin.game.application.common.game.GameWorldSpawnEntityService;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.spatial.Grid;
import com.blaj.openmetin.game.infrastructure.service.map.AtlasMapProviderService;
import com.blaj.openmetin.game.infrastructure.service.map.MapAttributeProviderService;
import com.blaj.openmetin.game.infrastructure.service.map.SpawnPointFileLoaderService;
import java.util.HashMap;
import java.util.Optional;
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

  private final java.util.Map<String, GameCharacterEntity> gameCharacterEntities = new HashMap<>();
  private final Grid<Map> mapDataGrid = new Grid<>(Map.class, 0, 0);
  @Getter private final java.util.Map<String, Map> maps = new HashMap<>();

  public void loadMaps() {
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

  private void addGameCharacterEntity(GameCharacterEntity gameCharacterEntity) {
    var name = gameCharacterEntity.getCharacterDto().getName();

    if (gameCharacterEntities.containsKey(name)) {
      gameCharacterEntities.replace(name, gameCharacterEntity);
    } else {
      gameCharacterEntities.put(name, gameCharacterEntity);
    }
  }
}
