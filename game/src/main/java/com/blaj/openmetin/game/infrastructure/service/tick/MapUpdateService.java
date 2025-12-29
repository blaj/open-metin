package com.blaj.openmetin.game.infrastructure.service.tick;

import com.blaj.openmetin.game.infrastructure.service.world.GameWorldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapUpdateService {

  private final GameWorldService gameWorldService;
  private final EntityUpdateService entityUpdateService;
  private final EntitySpawnService entitySpawnService;
  private final EntityDespawnService entityDespawnService;

  public void update() {
    var maps = gameWorldService.getMaps();

    maps.forEach(
        (mapName, map) -> {
          entitySpawnService.processPendingSpawns(map);
          entityDespawnService.processPendingRemovals(map);
          entityUpdateService.update(map);
        });
  }
}
