package com.blaj.openmetin.game.application.common.animation;

import com.blaj.openmetin.game.domain.enums.animation.AnimationSubType;
import com.blaj.openmetin.game.domain.enums.animation.AnimationType;
import com.blaj.openmetin.game.domain.model.animation.Animation;
import java.util.Optional;

public interface AnimationProviderService {

  Optional<Animation> getAnimation(
      long entityId, AnimationType animationType, AnimationSubType animationSubType);
}
