package com.blaj.openmetin.game.infrastructure.service.tick;

import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import org.springframework.stereotype.Service;

@Service
public class NearbyEntityUpdateService {

  public void addNearbyEntity(BaseGameEntity entity, BaseGameEntity nearbyEntity) {
    entity.addNearbyEntity(nearbyEntity);
    nearbyEntity.addNearbyEntity(entity);
  }

  public void removeNearbyEntity(BaseGameEntity entity, BaseGameEntity nearbyEntity) {
    entity.removeNearbyEntity(nearbyEntity);
    nearbyEntity.removeNearbyEntity(entity);
  }
}
