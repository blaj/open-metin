package com.blaj.openmetin.game.infrastructure.service.tick;

import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import org.springframework.stereotype.Service;

@Service
public class EntitySpawnService {

  public void processPendingSpawns(Map map) {
    var pendingSpawns = map.getPendingSpawns();

    BaseGameEntity entity;
    while ((entity = pendingSpawns.poll()) != null) {

    }
  }
}
