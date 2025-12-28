package com.blaj.openmetin.game.infrastructure.service.tick;

import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapUpdateService {

  private static final int VIEW_DISTANCE = 5000;

  private final ThreadLocal<List<BaseGameEntity>> nearbyBuffer =
      ThreadLocal.withInitial(ArrayList::new);
  private final ThreadLocal<Set<BaseGameEntity>> removeBuffer =
      ThreadLocal.withInitial(HashSet::new);

  private final NearbyEntityUpdateService nearbyEntityUpdateService;
  private final EntitySpawnService entitySpawnService;
  private final EntityDespawnService entityDespawnService;

  public void updateMap(Map map) {
    entitySpawnService.processPendingSpawns(map);
    entityDespawnService.processPendingRemovals(map);
  }
}
