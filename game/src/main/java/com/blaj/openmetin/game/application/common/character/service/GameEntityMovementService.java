package com.blaj.openmetin.game.application.common.character.service;

import com.blaj.openmetin.game.domain.enums.animation.AnimationSubType;
import com.blaj.openmetin.game.domain.enums.animation.AnimationType;
import com.blaj.openmetin.game.domain.enums.entity.EntityState;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.shared.common.utils.MathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameEntityMovementService {

  private final AnimationProviderService animationProviderService;

  public void goTo(BaseGameEntity baseGameEntity, int x, int y, long startAt) {
    if (baseGameEntity.getPositionX() == x && baseGameEntity.getPositionY() == y) {
      return;
    }

    if (baseGameEntity.getTargetPositionX() == x && baseGameEntity.getTargetPositionY() == y) {
      return;
    }

    var animation =
        animationProviderService.getAnimation(
            baseGameEntity.getEntityClass(), AnimationType.RUN, AnimationSubType.GENERAL);

    baseGameEntity.setState(EntityState.MOVING);
    baseGameEntity.setTargetPositionX(x);
    baseGameEntity.setTargetPositionY(y);
    baseGameEntity.setStartPositionX(baseGameEntity.getPositionX());
    baseGameEntity.setStartPositionY(baseGameEntity.getPositionY());
    baseGameEntity.setMovementStartAt(startAt);

    var distance =
        MathUtils.distance(
            baseGameEntity.getStartPositionX(),
            baseGameEntity.getStartPositionY(),
            baseGameEntity.getTargetPositionX(),
            baseGameEntity.getTargetPositionY());

    var movementDuration =
        animation
            .map(
                anim -> {
                  var animationSpeed = -anim.accumulationY() / anim.duration();
                  var i = 100 - baseGameEntity.getMovementSpeed();

                  if (i > 0) {
                    i = 100 + i;
                  } else if (i < 0) {
                    i = 10000 / (100 - i);
                  } else {
                    i = 100;
                  }

                  return (long) ((distance / animationSpeed) * 1000) * i / 100;
                })
            .orElse(0L);

    baseGameEntity.setMovementDuration(movementDuration);
  }

  public void update() {

  }
}
