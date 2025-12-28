package com.blaj.openmetin.game.infrastructure.service.tick;

import com.blaj.openmetin.game.application.common.character.service.GameEntityMovementService;
import com.blaj.openmetin.game.infrastructure.service.world.GameWorldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntityUpdateService {

  private final GameWorldService gameWorldService;
  private final MapUpdateService mapUpdateService;
  private final GameEntityMovementService gameEntityMovementService;

  public void updateAll() {
    var maps = gameWorldService.getMaps();

    maps.forEach(
        (mapName, map) -> {
          mapUpdateService.updateMap(map);
          gameEntityMovementService.update();
        });
  }
}
