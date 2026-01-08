package com.blaj.openmetin.game.infrastructure.service.tick;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.game.application.common.entity.EntityVisibilityService;
import com.blaj.openmetin.game.domain.enums.entity.EntityType;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.domain.model.Coordinates;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EntityDespawnServiceTest {

  private EntityDespawnService entityDespawnService;

  @Mock private EntityVisibilityService entityVisibilityService;

  @Mock private BaseGameEntity entity1;
  @Mock private BaseGameEntity entity2;
  @Mock private GameCharacterEntity gameCharacterEntity;
  @Mock private GameSession session;

  @BeforeEach
  public void beforeEach() {
    entityDespawnService = new EntityDespawnService(entityVisibilityService);
  }

  @Test
  public void givenNoPendingRemovals_whenProcessPendingRemovals_thenDoNothing() {
    // given
    var map = new Map("test", new Coordinates(0, 0), 1, 1, null);

    // when
    entityDespawnService.processPendingRemovals(map);

    // then
    assertThat(map.getEntities()).isEmpty();

    then(entityVisibilityService).should(never()).hideEntityFromPlayer(any(), anyLong());
  }

  @Test
  public void givenEntityWithoutNearbyEntities_whenProcessPendingRemovals_thenRemoveEntity() {
    // given
    var map = new Map("test", new Coordinates(0, 0), 1, 1, null);
    given(entity1.getPositionX()).willReturn(1000);
    given(entity1.getPositionY()).willReturn(1000);

    map.getQuadTree().insert(entity1);
    map.getEntities().add(entity1);
    map.getPendingRemovals().add(entity1);

    // when
    entityDespawnService.processPendingRemovals(map);

    // then
    assertThat(map.getEntities()).isEmpty();
    assertThat(map.getPendingRemovals()).isEmpty();

    then(entity1).should().setMap(null);
  }

  @Test
  public void
      givenEntityWithNearbyNonPlayerEntity_whenProcessPendingRemovals_thenRemoveFromBothSides() {
    // given
    var map = new Map("test", new Coordinates(0, 0), 1, 1, null);
    var nearbyEntities = new HashSet<BaseGameEntity>();
    nearbyEntities.add(entity2);

    given(entity1.getPositionX()).willReturn(1000);
    given(entity1.getPositionY()).willReturn(1000);
    given(entity1.getNearbyEntities()).willReturn(nearbyEntities);
    given(entity2.getType()).willReturn(EntityType.NPC);

    map.getQuadTree().insert(entity1);
    map.getEntities().add(entity1);
    map.getPendingRemovals().add(entity1);

    // when
    entityDespawnService.processPendingRemovals(map);

    // then
    assertThat(map.getEntities()).isEmpty();
    assertThat(entity1.getNearbyEntities()).isEmpty();

    then(entity2).should().removeNearbyEntity(entity1);
    then(entity1).should().setMap(null);
    then(entityVisibilityService).should(never()).hideEntityFromPlayer(any(), anyLong());
  }

  @Test
  public void givenEntityWithNearbyPlayer_whenProcessPendingRemovals_thenHideFromPlayer() {
    // given
    var map = new Map("test", new Coordinates(0, 0), 1, 1, null);
    var nearbyEntities = new HashSet<BaseGameEntity>();
    nearbyEntities.add(gameCharacterEntity);

    given(entity1.getPositionX()).willReturn(1000);
    given(entity1.getPositionY()).willReturn(1000);
    given(entity1.getNearbyEntities()).willReturn(nearbyEntities);
    given(gameCharacterEntity.getType()).willReturn(EntityType.PLAYER);
    given(gameCharacterEntity.getSession()).willReturn(session);
    given(session.getId()).willReturn(123L);

    map.getQuadTree().insert(entity1);
    map.getEntities().add(entity1);
    map.getPendingRemovals().add(entity1);

    // when
    entityDespawnService.processPendingRemovals(map);

    // then
    assertThat(map.getEntities()).isEmpty();
    assertThat(entity1.getNearbyEntities()).isEmpty();

    then(gameCharacterEntity).should().removeNearbyEntity(entity1);
    then(entity1).should().setMap(null);
    then(entityVisibilityService).should().hideEntityFromPlayer(entity1, 123L);
  }

  @Test
  public void givenMultipleEntities_whenProcessPendingRemovals_thenRemoveAll() {
    // given
    var map = new Map("test", new Coordinates(0, 0), 1, 1, null);

    given(entity1.getPositionX()).willReturn(1000);
    given(entity1.getPositionY()).willReturn(1000);
    given(entity2.getPositionX()).willReturn(2000);
    given(entity2.getPositionY()).willReturn(2000);

    map.getQuadTree().insert(entity1);
    map.getQuadTree().insert(entity2);
    map.getEntities().add(entity1);
    map.getEntities().add(entity2);
    map.getPendingRemovals().add(entity1);
    map.getPendingRemovals().add(entity2);

    // when
    entityDespawnService.processPendingRemovals(map);

    // then
    assertThat(map.getEntities()).isEmpty();
    assertThat(map.getPendingRemovals()).isEmpty();

    then(entity1).should().setMap(null);
    then(entity2).should().setMap(null);
  }
}
