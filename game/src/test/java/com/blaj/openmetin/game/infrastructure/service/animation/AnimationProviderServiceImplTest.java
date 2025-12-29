package com.blaj.openmetin.game.infrastructure.service.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.blaj.openmetin.game.domain.enums.animation.AnimationSubType;
import com.blaj.openmetin.game.domain.enums.animation.AnimationType;
import com.blaj.openmetin.game.domain.model.animation.Animation;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AnimationProviderServiceImplTest {

  private AnimationProviderServiceImpl animationProviderService;

  @Mock private AnimationFileLoaderService animationFileLoaderService;
  @Mock private AnimationCacheService animationCacheService;
  @Mock private DataPathProperties dataPathProperties;

  @Mock private Animation animation;

  @BeforeEach
  public void beforeEach() {
    animationProviderService =
        new AnimationProviderServiceImpl(
            animationFileLoaderService, animationCacheService, dataPathProperties);
  }

  @Test
  public void givenAnimationFilesNotFound_whenLoadAnimations_thenDoNotCacheAnimations() {
    // given
    given(dataPathProperties.baseDirectory()).willReturn(Path.of("/data"));
    given(animationFileLoaderService.loadAnimation(any(Path.class))).willReturn(Optional.empty());

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationCacheService)
        .should(never())
        .put(any(Long.class), any(AnimationType.class), any(AnimationSubType.class), any());
  }

  @Test
  public void givenAnimationFiles_whenLoadAnimations_thenLoadAndCacheAllCharacterAnimations() {
    // given
    given(dataPathProperties.baseDirectory()).willReturn(Path.of("/data"));
    given(animationFileLoaderService.loadAnimation(any(Path.class)))
        .willReturn(Optional.of(animation));

    // when
    animationProviderService.loadAnimations();

    // then
    then(animationCacheService)
        .should(times(16))
        .put(any(Long.class), any(AnimationType.class), any(AnimationSubType.class), eq(animation));

    then(animationFileLoaderService)
        .should()
        .loadAnimation(Path.of("/data/pc/warrior/general/walk.msa"));
    then(animationFileLoaderService)
        .should()
        .loadAnimation(Path.of("/data/pc2/assassin/general/run.msa"));

    then(animationCacheService)
        .should()
        .put(eq(0L), eq(AnimationType.WALK), eq(AnimationSubType.GENERAL), eq(animation));
    then(animationCacheService)
        .should()
        .put(eq(5L), eq(AnimationType.RUN), eq(AnimationSubType.GENERAL), eq(animation));
  }

  @Test
  public void givenNoAnimation_whenGetAnimation_thenReturnEmpty() {
    // given
    var entityId = 1L;
    var animationType = AnimationType.RUN;
    var animationSubType = AnimationSubType.GENERAL;

    given(animationCacheService.get(entityId, animationType, animationSubType))
        .willReturn(Optional.empty());

    // when
    var result = animationProviderService.getAnimation(entityId, animationType, animationSubType);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenAnimation_whenGetAnimation_thenReturnAnimation() {
    // given
    var entityId = 1L;
    var animationType = AnimationType.RUN;
    var animationSubType = AnimationSubType.GENERAL;

    given(animationCacheService.get(entityId, animationType, animationSubType))
        .willReturn(Optional.of(animation));

    // when
    var result = animationProviderService.getAnimation(entityId, animationType, animationSubType);

    // then
    assertThat(result).contains(animation);
  }
}
