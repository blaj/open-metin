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
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.game.domain.model.spatial.QuadTree;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EntitySpawnServiceTest {

  private EntitySpawnService entitySpawnService;

  @Mock private EntityVisibilityService entityVisibilityService;

  @Mock private Map map;
  @Mock private QuadTree quadTree;
  @Mock private BaseGameEntity entity1;
  @Mock private BaseGameEntity entity2;
  @Mock private GameCharacterEntity player1;
  @Mock private GameCharacterEntity player2;
  @Mock private GameSession session1;
  @Mock private GameSession session2;

  private ConcurrentLinkedQueue<BaseGameEntity> pendingSpawns;
  private List<BaseGameEntity> entities;

  @BeforeEach
  public void beforeEach() {
    entitySpawnService = new EntitySpawnService(entityVisibilityService);
    pendingSpawns = new ConcurrentLinkedQueue<>();
    entities = new ArrayList<>();

    given(map.getPendingSpawns()).willReturn(pendingSpawns);
    given(map.getQuadTree()).willReturn(quadTree);
    given(map.getEntities()).willReturn(entities);
  }

  @Test
  public void givenNoPendingSpawns_whenProcessPendingSpawns_thenDoNothing() {
    // when
    entitySpawnService.processPendingSpawns(map);

    // then
    then(quadTree).should(never()).insert(any());
    then(entityVisibilityService).should(never()).showEntityToPlayer(any(), anyLong());
  }

  @Test
  public void givenEntityOutsideQuadTree_whenProcessPendingSpawns_thenDoNotAddToEntities() {
    // given
    pendingSpawns.add(entity1);

    given(quadTree.insert(entity1)).willReturn(false);

    // when
    entitySpawnService.processPendingSpawns(map);

    // then
    then(quadTree).should().insert(entity1);
    then(entity1).should(never()).setMap(any());
    then(quadTree).should(never()).queryAround(any(), anyInt(), anyInt(), anyInt(), any());
  }

  @Test
  public void givenEntityWithoutNearbyEntities_whenProcessPendingSpawns_thenAddToEntities() {
    // given
    pendingSpawns.add(entity1);

    given(quadTree.insert(entity1)).willReturn(true);
    given(entity1.getPositionX()).willReturn(1000);
    given(entity1.getPositionY()).willReturn(2000);
    given(entity1.getType()).willReturn(EntityType.NPC);

    // when
    entitySpawnService.processPendingSpawns(map);

    // then
    then(quadTree).should().insert(entity1);
    then(quadTree)
        .should()
        .queryAround(any(), eq(1000), eq(2000), eq(10000), eq(EntityType.PLAYER));
    then(entity1).should().setMap(map);
    then(entityVisibilityService).should(never()).showEntityToPlayer(any(), anyLong());
  }

  @Test
  public void givenNpcWithNearbyNpc_whenProcessPendingSpawns_thenAddNearbyButDoNotShow() {
    // given
    pendingSpawns.add(entity1);

    given(quadTree.insert(entity1)).willReturn(true);
    given(entity1.getPositionX()).willReturn(1000);
    given(entity1.getPositionY()).willReturn(2000);
    given(entity1.getType()).willReturn(EntityType.NPC);
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
    entitySpawnService.processPendingSpawns(map);

    // then
    then(entity1).should().addNearbyEntity(entity2);
    then(entity2).should().addNearbyEntity(entity1);
    then(entity1).should().setMap(map);
    then(entityVisibilityService).should(never()).showEntityToPlayer(any(), anyLong());
  }

  @Test
  public void givenPlayerWithNearbyNpc_whenProcessPendingSpawns_thenShowNpcToPlayer() {
    // given
    pendingSpawns.add(player1);

    given(quadTree.insert(player1)).willReturn(true);
    given(player1.getPositionX()).willReturn(1000);
    given(player1.getPositionY()).willReturn(2000);
    given(player1.getType()).willReturn(EntityType.PLAYER);
    given(player1.getSession()).willReturn(session1);
    given(entity2.getType()).willReturn(EntityType.NPC);
    given(session1.getId()).willReturn(123L);

    doAnswer(
            invocation -> {
              List<BaseGameEntity> list = invocation.getArgument(0);
              list.add(entity2);
              return null;
            })
        .when(quadTree)
        .queryAround(any(), anyInt(), anyInt(), anyInt(), any());

    // when
    entitySpawnService.processPendingSpawns(map);

    // then
    then(player1).should().addNearbyEntity(entity2);
    then(entity2).should().addNearbyEntity(player1);
    then(player1).should().setMap(map);
    then(entityVisibilityService).should().showEntityToPlayer(entity2, 123L);
  }

  @Test
  public void givenNpcWithNearbyPlayer_whenProcessPendingSpawns_thenShowNpcToPlayer() {
    // given
    pendingSpawns.add(entity1);

    given(quadTree.insert(entity1)).willReturn(true);
    given(entity1.getPositionX()).willReturn(1000);
    given(entity1.getPositionY()).willReturn(2000);
    given(entity1.getType()).willReturn(EntityType.NPC);
    given(player1.getType()).willReturn(EntityType.PLAYER);
    given(player1.getSession()).willReturn(session1);
    given(session1.getId()).willReturn(123L);

    doAnswer(
            invocation -> {
              List<BaseGameEntity> list = invocation.getArgument(0);
              list.add(player1);
              return null;
            })
        .when(quadTree)
        .queryAround(any(), anyInt(), anyInt(), anyInt(), any());

    // when
    entitySpawnService.processPendingSpawns(map);

    // then
    then(entity1).should().addNearbyEntity(player1);
    then(player1).should().addNearbyEntity(entity1);
    then(entity1).should().setMap(map);
    then(entityVisibilityService).should().showEntityToPlayer(entity1, 123L);
  }

  @Test
  public void givenPlayerWithNearbyPlayer_whenProcessPendingSpawns_thenShowBothPlayers() {
    // given
    pendingSpawns.add(player1);

    given(quadTree.insert(player1)).willReturn(true);
    given(player1.getPositionX()).willReturn(1000);
    given(player1.getPositionY()).willReturn(2000);
    given(player1.getType()).willReturn(EntityType.PLAYER);
    given(player1.getSession()).willReturn(session1);
    given(player2.getType()).willReturn(EntityType.PLAYER);
    given(player2.getSession()).willReturn(session2);
    given(session1.getId()).willReturn(123L);
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
    entitySpawnService.processPendingSpawns(map);

    // then
    then(player1).should().addNearbyEntity(player2);
    then(player2).should().addNearbyEntity(player1);
    then(player1).should().setMap(map);
    then(entityVisibilityService).should().showEntityToPlayer(player2, 123L);
    then(entityVisibilityService).should().showEntityToPlayer(player1, 456L);
  }

  @Test
  public void givenMultipleEntities_whenProcessPendingSpawns_thenProcessAll() {
    // given
    pendingSpawns.add(entity1);
    pendingSpawns.add(entity2);

    given(quadTree.insert(any())).willReturn(true);
    given(entity1.getPositionX()).willReturn(1000);
    given(entity1.getPositionY()).willReturn(2000);
    given(entity1.getType()).willReturn(EntityType.NPC);
    given(entity2.getPositionX()).willReturn(3000);
    given(entity2.getPositionY()).willReturn(4000);
    given(entity2.getType()).willReturn(EntityType.NPC);

    // when
    entitySpawnService.processPendingSpawns(map);

    // then
    then(quadTree).should().insert(entity1);
    then(quadTree).should().insert(entity2);
    then(entity1).should().setMap(map);
    then(entity2).should().setMap(map);
  }
}
