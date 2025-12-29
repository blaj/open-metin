package com.blaj.openmetin.game.application.common.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.game.application.common.animation.AnimationProviderService;
import com.blaj.openmetin.game.domain.enums.animation.AnimationSubType;
import com.blaj.openmetin.game.domain.enums.animation.AnimationType;
import com.blaj.openmetin.game.domain.enums.entity.EntityState;
import com.blaj.openmetin.game.domain.model.animation.Animation;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GameEntityMovementServiceTest {

  private GameEntityMovementService gameEntityMovementService;

  @Mock private AnimationProviderService animationProviderService;
  @Mock private Animation animation;

  @BeforeEach
  public void beforeEach() {
    gameEntityMovementService = new GameEntityMovementService(animationProviderService);
  }

  @Test
  public void givenXAndYEqualsPosition_whenGoTo_thenDoNothing() {
    // given
    var x = 123;
    var y = 321;
    var gameCharacterEntity = GameCharacterEntity.builder().positionX(x).positionY(y).build();

    // when
    gameEntityMovementService.goTo(gameCharacterEntity, x, y, 123L);

    // then
    then(animationProviderService).should(never()).getAnimation(anyLong(), any(), any());
  }

  @Test
  public void givenXAndYEqualsTargetPosition_whenGoTo_thenDoNothing() {
    // given
    var x = 123;
    var y = 321;
    var gameCharacterEntity =
        GameCharacterEntity.builder()
            .positionX(555)
            .positionY(555)
            .targetPositionX(x)
            .targetPositionY(y)
            .build();

    // when
    gameEntityMovementService.goTo(gameCharacterEntity, x, y, 123L);

    // then
    then(animationProviderService).should(never()).getAnimation(anyLong(), any(), any());
  }

  @Test
  public void givenOnlyXEqualsPosition_whenGoTo_thenProceed() {
    // given
    var x = 123;
    var gameCharacterEntity = GameCharacterEntity.builder().positionX(x).positionY(999).build();

    given(animationProviderService.getAnimation(anyLong(), any(), any()))
        .willReturn(Optional.empty());

    // when
    gameEntityMovementService.goTo(gameCharacterEntity, x, 321, 123L);

    // then
    then(animationProviderService).should().getAnimation(anyLong(), any(), any());
  }

  @Test
  public void givenOnlyYEqualsPosition_whenGoTo_thenProceed() {
    // given
    var y = 321;
    var gameCharacterEntity = GameCharacterEntity.builder().positionX(999).positionY(y).build();

    given(animationProviderService.getAnimation(anyLong(), any(), any()))
        .willReturn(Optional.empty());

    // when
    gameEntityMovementService.goTo(gameCharacterEntity, 123, y, 123L);

    // then
    then(animationProviderService).should().getAnimation(anyLong(), any(), any());
  }

  @Test
  public void givenOnlyXEqualsTargetPosition_whenGoTo_thenProceed() {
    // given
    var x = 123;
    var gameCharacterEntity =
        GameCharacterEntity.builder()
            .positionX(555)
            .positionY(555)
            .targetPositionX(x)
            .targetPositionY(999)
            .build();

    given(animationProviderService.getAnimation(anyLong(), any(), any()))
        .willReturn(Optional.empty());

    // when
    gameEntityMovementService.goTo(gameCharacterEntity, x, 321, 123L);

    // then
    then(animationProviderService).should().getAnimation(anyLong(), any(), any());
  }

  @Test
  public void givenOnlyYEqualsTargetPosition_whenGoTo_thenProceed() {
    // given
    var y = 321;
    var gameCharacterEntity =
        GameCharacterEntity.builder()
            .positionX(555)
            .positionY(555)
            .targetPositionX(999)
            .targetPositionY(y)
            .build();

    given(animationProviderService.getAnimation(anyLong(), any(), any()))
        .willReturn(Optional.empty());

    // when
    gameEntityMovementService.goTo(gameCharacterEntity, 123, y, 123L);

    // then
    then(animationProviderService).should().getAnimation(anyLong(), any(), any());
  }

  @Test
  public void givenNonExistingAnimation_whenGoTo_thenSetMovementDurationTo0() {
    // given
    var x = 123;
    var y = 321;
    var startAt = 123L;
    var entityClass = 333L;
    var gameCharacterEntity =
        GameCharacterEntity.builder()
            .positionX(x - 1)
            .positionY(y - 1)
            .targetPositionX(x - 2)
            .targetPositionY(y - 2)
            .entityClass(entityClass)
            .build();

    given(
            animationProviderService.getAnimation(
                entityClass, AnimationType.RUN, AnimationSubType.GENERAL))
        .willReturn(Optional.empty());

    // when
    gameEntityMovementService.goTo(gameCharacterEntity, x, y, startAt);

    // then
    assertThat(gameCharacterEntity.getState()).isEqualTo(EntityState.MOVING);
    assertThat(gameCharacterEntity.getTargetPositionX()).isEqualTo(x);
    assertThat(gameCharacterEntity.getTargetPositionY()).isEqualTo(y);
    assertThat(gameCharacterEntity.getStartPositionX())
        .isEqualTo(gameCharacterEntity.getPositionX());
    assertThat(gameCharacterEntity.getStartPositionY())
        .isEqualTo(gameCharacterEntity.getPositionY());
    assertThat(gameCharacterEntity.getMovementStartAt()).isEqualTo(startAt);
    assertThat(gameCharacterEntity.getMovementDuration()).isEqualTo(0L);
  }

  @ParameterizedTest
  @CsvSource({
    "50, 1500", // i > 0: i = 150
    "80, 1200", // i > 0: i = 120
    "100, 1000", // i == 0: i = 100
    "150, 660", // i < 0: i = 66
    "200, 500" // i < 0: i = 50
  })
  public void givenValid_whenGoTo_thenCalculateMovementDuration(
      short movementSpeed, long expectedDuration) {
    // given
    var startX = 0;
    var startY = 0;
    var targetX = 300;
    var targetY = 400;
    var startAt = 123L;
    var entityClass = 333L;

    var gameCharacterEntity =
        GameCharacterEntity.builder()
            .positionX(startX)
            .positionY(startY)
            .targetPositionX(targetX - 1)
            .targetPositionY(targetY - 1)
            .entityClass(entityClass)
            .movementSpeed(movementSpeed)
            .build();

    given(animation.accumulationY()).willReturn(-500.0f);
    given(animation.duration()).willReturn(1.0f);
    given(
            animationProviderService.getAnimation(
                entityClass, AnimationType.RUN, AnimationSubType.GENERAL))
        .willReturn(Optional.of(animation));

    // when
    gameEntityMovementService.goTo(gameCharacterEntity, targetX, targetY, startAt);

    // then
    assertThat(gameCharacterEntity.getState()).isEqualTo(EntityState.MOVING);
    assertThat(gameCharacterEntity.getTargetPositionX()).isEqualTo(targetX);
    assertThat(gameCharacterEntity.getTargetPositionY()).isEqualTo(targetY);
    assertThat(gameCharacterEntity.getStartPositionX())
        .isEqualTo(gameCharacterEntity.getPositionX());
    assertThat(gameCharacterEntity.getStartPositionY())
        .isEqualTo(gameCharacterEntity.getPositionY());
    assertThat(gameCharacterEntity.getMovementStartAt()).isEqualTo(startAt);
    assertThat(gameCharacterEntity.getMovementDuration()).isEqualTo(expectedDuration);
  }

  @Test
  public void givenValid_whenWait_thenSetPosition() {
    // given
    var x = 111;
    var y = 222;
    var gameCharacterEntity = GameCharacterEntity.builder().build();

    // when
    gameEntityMovementService.wait(gameCharacterEntity, x, y);

    // then
    assertThat(gameCharacterEntity.getPositionX()).isEqualTo(x);
    assertThat(gameCharacterEntity.getPositionY()).isEqualTo(y);
  }
}
