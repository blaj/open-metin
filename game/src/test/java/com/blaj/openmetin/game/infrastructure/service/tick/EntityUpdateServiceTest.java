package com.blaj.openmetin.game.infrastructure.service.tick;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.game.application.common.entity.EntityVisibilityService;
import com.blaj.openmetin.game.domain.enums.entity.EntityState;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.game.domain.model.spatial.QuadTree;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EntityUpdateServiceTest {
  private EntityUpdateService entityUpdateService;

  @Mock private EntityVisibilityService entityVisibilityService;

  @Mock private Map map;
  @Mock private QuadTree quadTree;
  @Mock private BaseGameEntity entity1;
  @Mock private BaseGameEntity entity2;
  @Mock private GameCharacterEntity player1;
  @Mock private GameCharacterEntity player2;
  @Mock private GameSession session1;
  @Mock private GameSession session2;

  private List<BaseGameEntity> entities;
  private Set<BaseGameEntity> nearbyEntities;

  @BeforeEach
  public void beforeEach() {
    DateTimeUtils.initialize();

    entityUpdateService = new EntityUpdateService(entityVisibilityService);
    entities = new ArrayList<>();
    nearbyEntities = new HashSet<>();

    given(map.getEntities()).willReturn(entities);
    given(map.getQuadTree()).willReturn(quadTree);
  }

  @Test
  public void givenNoEntities_whenUpdate_thenDoNothing() {
    // when
    entityUpdateService.update(map);

    // then
    then(quadTree).should(never()).updatePosition(any());
    then(entityVisibilityService).should(never()).showEntityToPlayer(any(), anyLong());
    then(entityVisibilityService).should(never()).hideEntityFromPlayer(any(), anyLong());
  }

  @Test
  public void givenEntityWithStateNotMoving_whenUpdate_thenDoNotUpdatePosition() {
    // given
    entities.add(entity1);

    given(entity1.getState()).willReturn(EntityState.IDLE);
    given(entity1.isPositionChanged()).willReturn(false);

    // when
    entityUpdateService.update(map);

    // then
    then(entity1).should(never()).setPositionX(anyInt());
    then(entity1).should(never()).setPositionY(anyInt());
  }

  @Test
  public void givenEntityWithStateMoving_whenUpdate_thenUpdatePosition() {
    // given
    entities.add(entity1);

    given(entity1.getState()).willReturn(EntityState.MOVING);
    given(entity1.getMovementStartAt()).willReturn(0L);
    given(entity1.getMovementDuration()).willReturn(1000L);
    given(entity1.getStartPositionX()).willReturn(0);
    given(entity1.getStartPositionY()).willReturn(0);
    given(entity1.getTargetPositionX()).willReturn(1000);
    given(entity1.getTargetPositionY()).willReturn(1000);
    given(entity1.isPositionChanged()).willReturn(false);

    // when
    entityUpdateService.update(map);

    // then
    then(entity1).should().setPositionX(anyInt());
    then(entity1).should().setPositionY(anyInt());
  }

  @Test
  public void givenEntityReachedTarget_whenUpdate_thenSetStateToIdle() {
    // given
    entities.add(entity1);

    given(entity1.getState()).willReturn(EntityState.MOVING);
    given(entity1.getMovementStartAt()).willReturn(0L);
    given(entity1.getMovementDuration()).willReturn(0L);
    given(entity1.getStartPositionX()).willReturn(0);
    given(entity1.getStartPositionY()).willReturn(0);
    given(entity1.getTargetPositionX()).willReturn(1000);
    given(entity1.getTargetPositionY()).willReturn(1000);
    given(entity1.isPositionChanged()).willReturn(false);

    // when
    entityUpdateService.update(map);

    // then
    then(entity1).should().setState(EntityState.IDLE);
  }

  @Test
  public void givenEntityPositionNotChanged_whenUpdate_thenDoNotUpdateQuadTree() {
    // given
    entities.add(entity1);

    given(entity1.getState()).willReturn(EntityState.IDLE);
    given(entity1.isPositionChanged()).willReturn(false);

    // when
    entityUpdateService.update(map);

    // then
    then(quadTree).should(never()).updatePosition(any());
  }

  @Test
  public void givenNonPlayerPositionChanged_whenUpdate_thenUpdateQuadTreeOnly() {
    // given
    entities.add(entity1);

    given(entity1.getState()).willReturn(EntityState.IDLE);
    given(entity1.isPositionChanged()).willReturn(true);
    given(entity1.getType()).willReturn(EntityType.NPC);

    // when
    entityUpdateService.update(map);

    // then
    then(entity1).should().setPositionChanged(false);
    then(quadTree).should().updatePosition(entity1);
    then(quadTree).should(never()).queryAround(any(), anyInt(), anyInt(), anyInt(), any());
  }

  @Test
  public void givenPlayerPositionChangedWithNoNearbyEntities_whenUpdate_thenQueryAround() {
    // given
    entities.add(player1);

    given(player1.getState()).willReturn(EntityState.IDLE);
    given(player1.isPositionChanged()).willReturn(true);
    given(player1.getType()).willReturn(EntityType.PLAYER);
    given(player1.getPositionX()).willReturn(1000);
    given(player1.getPositionY()).willReturn(2000);
    given(player1.getNearbyEntities()).willReturn(nearbyEntities);

    // when
    entityUpdateService.update(map);

    // then
    then(player1).should().setPositionChanged(false);
    then(quadTree).should().updatePosition(player1);
    then(quadTree).should().queryAround(any(), eq(1000), eq(2000), eq(10000), eq(null));
  }

  @Test
  public void givenPlayerPositionChangedWithRemovedNearbyEntity_whenUpdate_thenHideEntity() {
    // given
    entities.add(player1);
    nearbyEntities.add(entity2);

    given(player1.getState()).willReturn(EntityState.IDLE);
    given(player1.isPositionChanged()).willReturn(true);
    given(player1.getType()).willReturn(EntityType.PLAYER);
    given(player1.getPositionX()).willReturn(1000);
    given(player1.getPositionY()).willReturn(2000);
    given(player1.getNearbyEntities()).willReturn(nearbyEntities);
    given(player1.getSession()).willReturn(session1);
    given(session1.getId()).willReturn(123L);
    given(entity2.getType()).willReturn(EntityType.NPC);

    // when
    entityUpdateService.update(map);

    // then
    then(entity2).should().removeNearbyEntity(player1);
    then(player1).should().removeNearbyEntity(entity2);
    then(entityVisibilityService).should().hideEntityFromPlayer(entity2, 123L);
  }

  @Test
  public void givenPlayerPositionChangedWithRemovedNearbyPlayer_whenUpdate_thenHideBothPlayers() {
    // given
    entities.add(player1);
    nearbyEntities.add(player2);

    given(player1.getState()).willReturn(EntityState.IDLE);
    given(player1.isPositionChanged()).willReturn(true);
    given(player1.getType()).willReturn(EntityType.PLAYER);
    given(player1.getPositionX()).willReturn(1000);
    given(player1.getPositionY()).willReturn(2000);
    given(player1.getNearbyEntities()).willReturn(nearbyEntities);
    given(player1.getSession()).willReturn(session1);
    given(session1.getId()).willReturn(123L);
    given(player2.getType()).willReturn(EntityType.PLAYER);
    given(player2.getSession()).willReturn(session2);
    given(session2.getId()).willReturn(456L);

    // when
    entityUpdateService.update(map);

    // then
    then(player2).should().removeNearbyEntity(player1);
    then(player1).should().removeNearbyEntity(player2);
    then(entityVisibilityService).should().hideEntityFromPlayer(player2, 123L);
    then(entityVisibilityService).should().hideEntityFromPlayer(player1, 456L);
  }

  @Test
  public void givenPlayerPositionChangedWithNewNearbyEntity_whenUpdate_thenShowEntity() {
    // given
    entities.add(player1);

    given(player1.getState()).willReturn(EntityState.IDLE);
    given(player1.isPositionChanged()).willReturn(true);
    given(player1.getType()).willReturn(EntityType.PLAYER);
    given(player1.getPositionX()).willReturn(1000);
    given(player1.getPositionY()).willReturn(2000);
    given(player1.getNearbyEntities()).willReturn(nearbyEntities);
    given(player1.getSession()).willReturn(session1);
    given(session1.getId()).willReturn(123L);
    given(entity2.getType()).willReturn(EntityType.NPC);

    doAnswer(
            invocation -> {
              List<BaseGameEntity> list = invocation.getArgument(0);
              list.add(entity2);
              return null;
            })
        .when(quadTree)
        .queryAround(any(), anyInt(), anyInt(), anyInt(), any());

    // when
    entityUpdateService.update(map);

    // then
    then(entity2).should().addNearbyEntity(player1);
    then(player1).should().addNearbyEntity(entity2);
    then(entityVisibilityService).should().showEntityToPlayer(entity2, 123L);
  }

  @Test
  public void givenPlayerPositionChangedWithNewNearbyPlayer_whenUpdate_thenShowBothPlayers() {
    // given
    entities.add(player1);

    given(player1.getState()).willReturn(EntityState.IDLE);
    given(player1.isPositionChanged()).willReturn(true);
    given(player1.getType()).willReturn(EntityType.PLAYER);
    given(player1.getPositionX()).willReturn(1000);
    given(player1.getPositionY()).willReturn(2000);
    given(player1.getNearbyEntities()).willReturn(nearbyEntities);
    given(player1.getSession()).willReturn(session1);
    given(session1.getId()).willReturn(123L);
    given(player2.getType()).willReturn(EntityType.PLAYER);
    given(player2.getSession()).willReturn(session2);
    given(session2.getId()).willReturn(456L);

    doAnswer(
            invocation -> {
              List<BaseGameEntity> list = invocation.getArgument(0);
              list.add(player2);
              return null;
            })
        .when(quadTree)
        .queryAround(any(), anyInt(), anyInt(), anyInt(), any());

    // when
    entityUpdateService.update(map);

    // then
    then(player2).should().addNearbyEntity(player1);
    then(player1).should().addNearbyEntity(player2);
    then(entityVisibilityService).should().showEntityToPlayer(player2, 123L);
    then(entityVisibilityService).should().showEntityToPlayer(player1, 456L);
  }

  @Test
  public void givenMultipleEntities_whenUpdate_thenUpdateAll() {
    // given
    entities.add(entity1);
    entities.add(entity2);

    given(entity1.getState()).willReturn(EntityState.IDLE);
    given(entity1.isPositionChanged()).willReturn(false);
    given(entity2.getState()).willReturn(EntityState.IDLE);
    given(entity2.isPositionChanged()).willReturn(false);

    // when
    entityUpdateService.update(map);

    // then
    then(entity1).should().getState();
    then(entity2).should().getState();
  }
}
