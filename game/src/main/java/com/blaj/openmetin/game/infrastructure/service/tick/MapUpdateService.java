package com.blaj.openmetin.game.infrastructure.service.tick;

import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.infrastructure.service.world.GameWorldService;
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
public class MapUpdateService {

  private static final int VIEW_DISTANCE = 5000;

  private final ThreadLocal<List<BaseGameEntity>> nearbyBuffer =
      ThreadLocal.withInitial(ArrayList::new);
  private final ThreadLocal<Set<BaseGameEntity>> removeBuffer =
      ThreadLocal.withInitial(HashSet::new);

  private final GameWorldService gameWorldService;
  private final EntityUpdateService entityUpdateService;
  private final NearbyEntityUpdateService nearbyEntityUpdateService;
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
