package com.blaj.openmetin.game.infrastructure.service.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.blaj.openmetin.game.application.common.entity.GameEntityMovementService;
import com.blaj.openmetin.game.domain.enums.entity.EntityState;
import com.blaj.openmetin.game.domain.enums.map.MapAttribute;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.entity.MonsterGameEntity;
import com.blaj.openmetin.game.domain.model.entity.MonsterGameEntity.BehaviourState;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import com.blaj.openmetin.shared.domain.model.Coordinates;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MonsterGameEntityBehaviourServiceImplTest {

  private static final long CURRENT_TIME = 1000L;
  private static final EnumSet<MapAttribute> BLOCK_ATTRIBUTES =
      EnumSet.of(MapAttribute.BLOCK, MapAttribute.OBJECT);

  private MonsterGameEntityBehaviourServiceImpl monsterGameEntityBehaviourService;

  @Mock private GameEntityMovementService gameEntityMovementService;
  @Mock private SessionService sessionService;

  @Mock private MonsterGameEntity monsterGameEntity;
  @Mock private GameCharacterEntity gameCharacterEntity;
  @Mock private BehaviourState behaviourState;
  @Mock private Map map;

  @BeforeEach
  public void beforeEach() {
    monsterGameEntityBehaviourService =
        new MonsterGameEntityBehaviourServiceImpl(gameEntityMovementService, sessionService);
  }

  @Test
  public void givenMonsterNotIdle_whenUpdate_thenDoesNotMove() {
    // given
    given(monsterGameEntity.getState()).willReturn(EntityState.MOVING);

    try (MockedStatic<DateTimeUtils> dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(CURRENT_TIME);

      // when
      monsterGameEntityBehaviourService.update(monsterGameEntity);

      // then
      then(behaviourState).should(never()).getNextMovementTime();
      then(gameEntityMovementService).should(never()).goTo(any(), anyInt(), anyInt(), anyLong());
    }
  }

  @Test
  public void givenMonsterIdleButTimeNotReached_whenUpdate_thenDoesNotMove() {
    // given
    given(monsterGameEntity.getBehaviourState()).willReturn(behaviourState);
    given(monsterGameEntity.getState()).willReturn(EntityState.IDLE);
    given(behaviourState.getNextMovementTime()).willReturn(CURRENT_TIME + 5000);

    try (MockedStatic<DateTimeUtils> dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(CURRENT_TIME);

      // when
      monsterGameEntityBehaviourService.update(monsterGameEntity);

      // then
      then(gameEntityMovementService).should(never()).goTo(any(), anyInt(), anyInt(), anyLong());
    }
  }

  @Test
  public void givenPositionOutsideMap_whenUpdate_thenDoesNotMove() {
    // given
    given(monsterGameEntity.getBehaviourState()).willReturn(behaviourState);
    given(monsterGameEntity.getMap()).willReturn(map);
    given(monsterGameEntity.getState()).willReturn(EntityState.IDLE);
    given(behaviourState.getNextMovementTime()).willReturn(CURRENT_TIME - 1000);
    given(monsterGameEntity.getCoordinates()).willReturn(new Coordinates(10000, 10000));
    given(map.isPositionInside(any(Coordinates.class))).willReturn(false);

    try (MockedStatic<DateTimeUtils> dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(CURRENT_TIME);

      // when
      monsterGameEntityBehaviourService.update(monsterGameEntity);

      // then
      then(map).should(times(16)).isPositionInside(any(Coordinates.class));
      then(gameEntityMovementService).should(never()).goTo(any(), anyInt(), anyInt(), anyLong());
    }
  }

  @Test
  public void givenPositionWithBlockAttribute_whenUpdate_thenDoesNotMove() {
    // given
    given(monsterGameEntity.getBehaviourState()).willReturn(behaviourState);
    given(monsterGameEntity.getMap()).willReturn(map);
    given(monsterGameEntity.getState()).willReturn(EntityState.IDLE);
    given(behaviourState.getNextMovementTime()).willReturn(CURRENT_TIME - 1000);
    given(monsterGameEntity.getCoordinates()).willReturn(new Coordinates(10000, 10000));
    given(map.isPositionInside(any(Coordinates.class))).willReturn(true);
    given(map.hasAnyMapAttributeOnCoordinates(any(Coordinates.class), eq(BLOCK_ATTRIBUTES)))
        .willReturn(true);

    try (MockedStatic<DateTimeUtils> dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(CURRENT_TIME);

      // when
      monsterGameEntityBehaviourService.update(monsterGameEntity);

      // then
      then(map)
          .should(times(16))
          .hasAnyMapAttributeOnCoordinates(any(Coordinates.class), eq(BLOCK_ATTRIBUTES));
      then(gameEntityMovementService).should(never()).goTo(any(), anyInt(), anyInt(), anyLong());
    }
  }

  @Test
  public void givenBlockOnPath_whenUpdate_thenDoesNotMove() {
    // given
    given(monsterGameEntity.getBehaviourState()).willReturn(behaviourState);
    given(monsterGameEntity.getMap()).willReturn(map);
    given(monsterGameEntity.getState()).willReturn(EntityState.IDLE);
    given(behaviourState.getNextMovementTime()).willReturn(CURRENT_TIME - 1000);
    given(monsterGameEntity.getCoordinates()).willReturn(new Coordinates(10000, 10000));
    given(map.isPositionInside(any(Coordinates.class))).willReturn(true);
    given(map.hasAnyMapAttributeOnCoordinates(any(Coordinates.class), eq(BLOCK_ATTRIBUTES)))
        .willReturn(false);
    given(
            map.hasAttributeOnStraightPath(
                any(Coordinates.class), any(Coordinates.class), eq(BLOCK_ATTRIBUTES)))
        .willReturn(true);

    try (MockedStatic<DateTimeUtils> dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(CURRENT_TIME);

      // when
      monsterGameEntityBehaviourService.update(monsterGameEntity);

      // then
      then(map)
          .should(times(16))
          .hasAttributeOnStraightPath(
              any(Coordinates.class), any(Coordinates.class), eq(BLOCK_ATTRIBUTES));
      then(gameEntityMovementService).should(never()).goTo(any(), anyInt(), anyInt(), anyLong());
    }
  }

  @Test
  public void givenValidConditions_whenUpdate_thenMovesMonster() {
    // given
    var session = new Session(123L, null);

    given(monsterGameEntity.getBehaviourState()).willReturn(behaviourState);
    given(monsterGameEntity.getMap()).willReturn(map);
    given(monsterGameEntity.getState()).willReturn(EntityState.IDLE);
    given(behaviourState.getNextMovementTime()).willReturn(CURRENT_TIME - 1000);
    given(monsterGameEntity.getCoordinates()).willReturn(new Coordinates(10000, 10000));
    given(monsterGameEntity.getPositionX()).willReturn(10000);
    given(monsterGameEntity.getPositionY()).willReturn(10000);
    given(monsterGameEntity.getTargetPositionX()).willReturn(10500);
    given(monsterGameEntity.getTargetPositionY()).willReturn(10500);
    given(monsterGameEntity.getVid()).willReturn(12345L);
    given(monsterGameEntity.getRotation()).willReturn(90.0f);
    given(monsterGameEntity.getMovementDuration()).willReturn(2000L);
    given(monsterGameEntity.getNearbyEntities()).willReturn(Set.of(gameCharacterEntity));

    given(map.isPositionInside(any(Coordinates.class))).willReturn(true);
    given(map.hasAnyMapAttributeOnCoordinates(any(Coordinates.class), eq(BLOCK_ATTRIBUTES)))
        .willReturn(false);
    given(
            map.hasAttributeOnStraightPath(
                any(Coordinates.class), any(Coordinates.class), eq(BLOCK_ATTRIBUTES)))
        .willReturn(false);

    given(gameCharacterEntity.getSession()).willReturn(session);

    try (MockedStatic<DateTimeUtils> dateTimeUtilsMock = mockStatic(DateTimeUtils.class)) {
      dateTimeUtilsMock.when(DateTimeUtils::getUnixTime).thenReturn(CURRENT_TIME);

      // when
      monsterGameEntityBehaviourService.update(monsterGameEntity);

      // then
      then(gameEntityMovementService)
          .should()
          .goTo(eq(monsterGameEntity), anyInt(), anyInt(), anyLong());
      then(monsterGameEntity).should().setRotation(anyFloat());
      then(sessionService).should().sendPacketAsync(eq(123L), any());
      then(behaviourState).should().setNextMovementTime(anyLong());
    }
  }

  @Test
  public void whenGetSupportedEntityClass_thenReturnsMonsterGameEntityClass() {
    // when
    var result = monsterGameEntityBehaviourService.getSupportedEntityClass();

    // then
    assertThat(result).isEqualTo(MonsterGameEntity.class);
  }
}
