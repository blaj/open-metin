package com.blaj.openmetin.game.infrastructure.service.animation;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.domain.enums.animation.AnimationSubType;
import com.blaj.openmetin.game.domain.enums.animation.AnimationType;
import com.blaj.openmetin.game.domain.model.animation.Animation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AnimationCacheServiceTest {

  private AnimationCacheService animationCacheService;

  @Mock private Animation animation1;
  @Mock private Animation animation2;

  @BeforeEach
  public void setUp() {
    animationCacheService = new AnimationCacheService();
  }

  @Test
  public void givenAnimation_whenPut_thenStoreAnimation() {
    // given
    var entityId = 1L;
    var animationType = AnimationType.RUN;
    var animationSubType = AnimationSubType.GENERAL;

    // when
    animationCacheService.put(entityId, animationType, animationSubType, animation1);
    var result = animationCacheService.get(entityId, animationType, animationSubType);

    // then
    assertThat(result).contains(animation1);
  }

  @Test
  public void givenExistingAnimation_whenPut_thenOverwriteAnimation() {
    // given
    var entityId = 1L;
    var animationType = AnimationType.RUN;
    var animationSubType = AnimationSubType.GENERAL;

    animationCacheService.put(entityId, animationType, animationSubType, animation1);

    // when
    animationCacheService.put(entityId, animationType, animationSubType, animation2);
    var result = animationCacheService.get(entityId, animationType, animationSubType);

    // then
    assertThat(result).contains(animation2);
  }

  @Test
  public void givenMultipleAnimations_whenPut_thenStoreAllAnimations() {
    // given
    var entityId = 1L;

    // when
    animationCacheService.put(entityId, AnimationType.RUN, AnimationSubType.GENERAL, animation1);
    animationCacheService.put(entityId, AnimationType.WALK, AnimationSubType.GENERAL, animation2);

    // then
    assertThat(animationCacheService.get(entityId, AnimationType.RUN, AnimationSubType.GENERAL))
        .contains(animation1);
    assertThat(animationCacheService.get(entityId, AnimationType.WALK, AnimationSubType.GENERAL))
        .contains(animation2);
  }

  @Test
  public void givenMultipleEntities_whenPut_thenStoreAnimationsForEachEntity() {
    // given
    var entityId1 = 1L;
    var entityId2 = 2L;
    var animationType = AnimationType.RUN;
    var animationSubType = AnimationSubType.GENERAL;

    // when
    animationCacheService.put(entityId1, animationType, animationSubType, animation1);
    animationCacheService.put(entityId2, animationType, animationSubType, animation2);

    // then
    assertThat(animationCacheService.get(entityId1, animationType, animationSubType))
        .contains(animation1);
    assertThat(animationCacheService.get(entityId2, animationType, animationSubType))
        .contains(animation2);
  }

  @Test
  public void givenNoAnimation_whenGet_thenReturnEmpty() {
    // given
    var entityId = 1L;
    var animationType = AnimationType.RUN;
    var animationSubType = AnimationSubType.GENERAL;

    // when
    var result = animationCacheService.get(entityId, animationType, animationSubType);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenDifferentEntityId_whenGet_thenReturnEmpty() {
    // given
    var entityId = 1L;
    var differentEntityId = 2L;
    var animationType = AnimationType.RUN;
    var animationSubType = AnimationSubType.GENERAL;

    animationCacheService.put(entityId, animationType, animationSubType, animation1);

    // when
    var result = animationCacheService.get(differentEntityId, animationType, animationSubType);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenDifferentAnimationType_whenGet_thenReturnEmpty() {
    // given
    var entityId = 1L;
    var animationType = AnimationType.RUN;
    var differentAnimationType = AnimationType.WALK;
    var animationSubType = AnimationSubType.GENERAL;

    animationCacheService.put(entityId, animationType, animationSubType, animation1);

    // when
    var result = animationCacheService.get(entityId, differentAnimationType, animationSubType);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenAnimation_whenGet_thenReturnAnimation() {
    // given
    var entityId = 1L;
    var animationType = AnimationType.RUN;
    var animationSubType = AnimationSubType.GENERAL;

    animationCacheService.put(entityId, animationType, animationSubType, animation1);

    // when
    var result = animationCacheService.get(entityId, animationType, animationSubType);

    // then
    assertThat(result).contains(animation1);
  }
}
