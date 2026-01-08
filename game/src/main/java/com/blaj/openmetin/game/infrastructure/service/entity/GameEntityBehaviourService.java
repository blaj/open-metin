package com.blaj.openmetin.game.infrastructure.service.entity;

import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;

public interface GameEntityBehaviourService<T extends BaseGameEntity> {

  void update(T gameEntity);

  Class<T> getSupportedEntityClass();
}
