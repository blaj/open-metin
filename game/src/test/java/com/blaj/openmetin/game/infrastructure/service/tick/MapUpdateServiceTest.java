package com.blaj.openmetin.game.infrastructure.service.tick;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.infrastructure.service.world.GameWorldService;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MapUpdateServiceTest {

  private MapUpdateService mapUpdateService;

  @Mock private GameWorldService gameWorldService;
  @Mock private EntityUpdateService entityUpdateService;
  @Mock private EntitySpawnService entitySpawnService;
  @Mock private EntityDespawnService entityDespawnService;

  @Mock private Map map1;
  @Mock private Map map2;
  @Mock private Map map3;

  @BeforeEach
  public void beforeEach() {
    mapUpdateService =
        new MapUpdateService(
            gameWorldService, entityUpdateService, entitySpawnService, entityDespawnService);
  }

  @Test
  public void givenNoMaps_whenUpdate_thenDoNothing() {
    // given
    given(gameWorldService.getMaps()).willReturn(new HashMap<>());

    // when
    mapUpdateService.update();

    // then
    then(entitySpawnService).should(never()).processPendingSpawns(any());
    then(entityDespawnService).should(never()).processPendingRemovals(any());
    then(entityUpdateService).should(never()).update(any());
  }

  @Test
  public void givenMultipleMaps_whenUpdate_thenProcessAllMaps() {
    // given
    var maps = new HashMap<String, Map>();
    maps.put("map1", map1);
    maps.put("map2", map2);
    maps.put("map3", map3);

    given(gameWorldService.getMaps()).willReturn(maps);

    // when
    mapUpdateService.update();

    // then
    then(entitySpawnService).should().processPendingSpawns(map1);
    then(entityDespawnService).should().processPendingRemovals(map1);
    then(entityUpdateService).should().update(map1);

    then(entitySpawnService).should().processPendingSpawns(map2);
    then(entityDespawnService).should().processPendingRemovals(map2);
    then(entityUpdateService).should().update(map2);

    then(entitySpawnService).should().processPendingSpawns(map3);
    then(entityDespawnService).should().processPendingRemovals(map3);
    then(entityUpdateService).should().update(map3);
  }
}
