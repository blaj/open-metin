package com.blaj.openmetin.game.infrastructure.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import com.blaj.openmetin.game.domain.model.entity.BaseGameEntity;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.map.Coordinates;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.map.MapAttributeSet;
import com.blaj.openmetin.game.domain.model.spawn.SpawnPoint;
import com.blaj.openmetin.game.infrastructure.service.map.AtlasMapProviderService;
import com.blaj.openmetin.game.infrastructure.service.map.MapAttributeProviderService;
import com.blaj.openmetin.game.infrastructure.service.map.SpawnPointFileLoaderService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GameWorldServiceTest {

  private GameWorldService gameWorldService;

  @Mock private AtlasMapProviderService atlasMapProviderService;
  @Mock private MapAttributeProviderService attributeProviderService;
  @Mock private SpawnPointFileLoaderService spawnPointFileLoaderService;

  @Mock private MapAttributeSet mapAttributeSet;
  @Mock private SpawnPoint spawnPoint;
  @Mock private BaseGameEntity baseGameEntity;
  @Mock private CharacterDto characterDto1;
  @Mock private CharacterDto characterDto2;
  @Mock private GameCharacterEntity gameCharacterEntity1;
  @Mock private GameCharacterEntity gameCharacterEntity2;

  @BeforeEach
  public void beforeEach() {
    gameWorldService =
        new GameWorldService(
            atlasMapProviderService, attributeProviderService, spawnPointFileLoaderService);
  }

  @Test
  public void givenNoMaps_whenLoadMaps_thenDoNotInitializeGrid() {
    // given
    given(atlasMapProviderService.getAll()).willReturn(List.of());

    // when
    gameWorldService.loadMaps();

    // then
    assertThat(gameWorldService.getMaps()).isEmpty();
    then(attributeProviderService)
        .should(never())
        .getAttributes(anyString(), any(), anyLong(), anyLong());
  }

  @Test
  public void givenMapsWithoutAttributesAndSpawnPoints_whenLoadMaps_thenLoadBasicMaps() {
    // given
    var map1 = new Map("map1", new Coordinates(0, 0), 4, 5, null);
    var map2 = new Map("map2", new Coordinates(102400, 128000), 3, 4, null);

    given(atlasMapProviderService.getAll()).willReturn(List.of(map1, map2));
    given(attributeProviderService.getAttributes(anyString(), any(), anyLong(), anyLong()))
        .willReturn(Optional.empty());
    given(spawnPointFileLoaderService.load(anyString())).willReturn(List.of());

    // when
    gameWorldService.loadMaps();

    // then
    assertThat(gameWorldService.getMaps()).hasSize(2);
    assertThat(gameWorldService.getMaps()).containsKeys("map1", "map2");

    then(attributeProviderService).should().getAttributes("map1", new Coordinates(0, 0), 4, 5);
    then(attributeProviderService)
        .should()
        .getAttributes("map2", new Coordinates(102400, 128000), 3, 4);
    then(spawnPointFileLoaderService).should().load("map1");
    then(spawnPointFileLoaderService).should().load("map2");
  }

  @Test
  public void givenMapsWithAttributesAndSpawnPoints_whenLoadMaps_thenLoadEverything() {
    // given
    var map = new Map("map", new Coordinates(0, 0), 4, 5, null);

    given(atlasMapProviderService.getAll()).willReturn(List.of(map));
    given(attributeProviderService.getAttributes("map", new Coordinates(0, 0), 4, 5))
        .willReturn(Optional.of(mapAttributeSet));
    given(spawnPointFileLoaderService.load("map")).willReturn(List.of(spawnPoint));

    // when
    gameWorldService.loadMaps();

    // then
    assertThat(gameWorldService.getMaps()).hasSize(1);
    assertThat(map.getMapAttributeSet()).isEqualTo(mapAttributeSet);
    assertThat(map.getSpawnPoints()).contains(spawnPoint);
  }

  @Test
  public void givenCoordinatesOutsideGrid_whenGetMap_thenReturnEmpty() {
    // given
    var map = new Map("map", new Coordinates(0, 0), 1, 1, null);

    given(atlasMapProviderService.getAll()).willReturn(List.of(map));
    given(attributeProviderService.getAttributes(anyString(), any(), anyLong(), anyLong()))
        .willReturn(Optional.empty());
    given(spawnPointFileLoaderService.load(anyString())).willReturn(List.of());

    gameWorldService.loadMaps();

    // when
    var result = gameWorldService.getMap(1000000, 1000000);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenCoordinatesInGridWithoutMap_whenGetMap_thenReturnEmpty() {
    // given
    var map = new Map("map", new Coordinates(0, 0), 1, 1, null);

    given(atlasMapProviderService.getAll()).willReturn(List.of(map));
    given(attributeProviderService.getAttributes(anyString(), any(), anyLong(), anyLong()))
        .willReturn(Optional.empty());
    given(spawnPointFileLoaderService.load(anyString())).willReturn(List.of());

    gameWorldService.loadMaps();

    // when
    var result = gameWorldService.getMap(50000, 50000);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenCoordinatesInGridWithMap_whenGetMap_thenReturnMap() {
    // given
    var map = new Map("map", new Coordinates(0, 0), 4, 5, null);

    given(atlasMapProviderService.getAll()).willReturn(List.of(map));
    given(attributeProviderService.getAttributes(anyString(), any(), anyLong(), anyLong()))
        .willReturn(Optional.empty());
    given(spawnPointFileLoaderService.load(anyString())).willReturn(List.of());

    gameWorldService.loadMaps();

    // when
    var result = gameWorldService.getMap(10000, 10000);

    // then
    assertThat(result).isPresent().contains(map);
  }

  @Test
  public void givenEntityOutsideMap_whenSpawnEntity_thenDoNothing() {
    // given
    var map = new Map("map", new Coordinates(0, 0), 1, 1, null);

    given(atlasMapProviderService.getAll()).willReturn(List.of(map));
    given(attributeProviderService.getAttributes(anyString(), any(), anyLong(), anyLong()))
        .willReturn(Optional.empty());
    given(spawnPointFileLoaderService.load(anyString())).willReturn(List.of());

    gameWorldService.loadMaps();

    given(baseGameEntity.getPositionX()).willReturn(1000000);
    given(baseGameEntity.getPositionY()).willReturn(1000000);

    // when
    gameWorldService.spawnEntity(baseGameEntity);

    // then
    assertThat(map.getPendingSpawns()).isEmpty();
  }

  @Test
  public void givenBaseGameEntityInMap_whenSpawnEntity_thenAddToPendingSpawns() {
    // given
    var map = new Map("map", new Coordinates(0, 0), 4, 5, null);

    given(atlasMapProviderService.getAll()).willReturn(List.of(map));
    given(attributeProviderService.getAttributes(anyString(), any(), anyLong(), anyLong()))
        .willReturn(Optional.empty());
    given(spawnPointFileLoaderService.load(anyString())).willReturn(List.of());

    gameWorldService.loadMaps();

    given(baseGameEntity.getPositionX()).willReturn(10000);
    given(baseGameEntity.getPositionY()).willReturn(10000);

    // when
    gameWorldService.spawnEntity(baseGameEntity);

    // then
    assertThat(map.getPendingSpawns()).hasSize(1).contains(baseGameEntity);
  }

  @Test
  public void
      givenGameCharacterEntityInMap_whenSpawnEntity_thenAddToPendingSpawnsAndCharacterEntities() {
    // given
    var map = new Map("map", new Coordinates(0, 0), 4, 5, null);

    given(atlasMapProviderService.getAll()).willReturn(List.of(map));
    given(attributeProviderService.getAttributes(anyString(), any(), anyLong(), anyLong()))
        .willReturn(Optional.empty());
    given(spawnPointFileLoaderService.load(anyString())).willReturn(List.of());

    gameWorldService.loadMaps();

    given(characterDto1.getName()).willReturn("TestPlayer");
    given(gameCharacterEntity1.getPositionX()).willReturn(10000);
    given(gameCharacterEntity1.getPositionY()).willReturn(10000);
    given(gameCharacterEntity1.getCharacterDto()).willReturn(characterDto1);

    // when
    gameWorldService.spawnEntity(gameCharacterEntity1);

    // then
    assertThat(map.getPendingSpawns()).hasSize(1).contains(gameCharacterEntity1);
  }

  @Test
  public void givenGameCharacterEntityWithSameName_whenSpawnEntity_thenReplaceExisting() {
    // given
    var map = new Map("map", new Coordinates(0, 0), 4, 5, null);

    given(atlasMapProviderService.getAll()).willReturn(List.of(map));
    given(attributeProviderService.getAttributes(anyString(), any(), anyLong(), anyLong()))
        .willReturn(Optional.empty());
    given(spawnPointFileLoaderService.load(anyString())).willReturn(List.of());

    gameWorldService.loadMaps();

    given(characterDto1.getName()).willReturn("TestPlayer");
    given(gameCharacterEntity1.getPositionX()).willReturn(10000);
    given(gameCharacterEntity1.getPositionY()).willReturn(10000);
    given(gameCharacterEntity1.getCharacterDto()).willReturn(characterDto1);

    given(characterDto2.getName()).willReturn("TestPlayer");
    given(gameCharacterEntity2.getPositionX()).willReturn(10000);
    given(gameCharacterEntity2.getPositionY()).willReturn(10000);
    given(gameCharacterEntity2.getCharacterDto()).willReturn(characterDto2);

    gameWorldService.spawnEntity(gameCharacterEntity1);

    // when
    gameWorldService.spawnEntity(gameCharacterEntity2);

    // then
    assertThat(map.getPendingSpawns())
        .hasSize(2)
        .contains(gameCharacterEntity1, gameCharacterEntity2);
  }
}
