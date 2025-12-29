package com.blaj.openmetin.game.infrastructure.service.map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.domain.model.map.Coordinates;
import com.blaj.openmetin.game.domain.model.map.MapAttributeSet;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MapAttributeProviderServiceTest {

  private MapAttributeProviderService mapAttributeProviderService;

  @Mock private DataPathProperties dataPathProperties;
  @Mock private MapAttributeFileLoaderService mapAttributeFileLoaderService;

  @Mock private MapAttributeSet mapAttributeSet;

  @BeforeEach
  public void beforeEach() {
    mapAttributeProviderService =
        new MapAttributeProviderService(dataPathProperties, mapAttributeFileLoaderService);
  }

  @Test
  public void givenMapAttributeFileNotFound_whenGetAttributes_thenReturnEmpty() {
    // given
    var mapName = "map1";
    var coordinates = new Coordinates(1000, 2000);
    var mapWidth = 4L;
    var mapHeight = 5L;
    var serverAttrPath = Path.of("/data/map/map1/server_attr");

    given(dataPathProperties.serverAttrFile(mapName)).willReturn(serverAttrPath);
    given(mapAttributeFileLoaderService.load(serverAttrPath, coordinates, mapWidth, mapHeight))
        .willReturn(Optional.empty());

    // when
    var result =
        mapAttributeProviderService.getAttributes(mapName, coordinates, mapWidth, mapHeight);

    // then
    assertThat(result).isEmpty();

    then(dataPathProperties).should().serverAttrFile(mapName);
    then(mapAttributeFileLoaderService)
        .should()
        .load(serverAttrPath, coordinates, mapWidth, mapHeight);
  }

  @Test
  public void givenMapAttributeFile_whenGetAttributes_thenReturnMapAttributeSet() {
    // given
    var mapName = "map1";
    var coordinates = new Coordinates(1000, 2000);
    var mapWidth = 4L;
    var mapHeight = 5L;
    var serverAttrPath = Path.of("/data/map/map1/server_attr");

    given(dataPathProperties.serverAttrFile(mapName)).willReturn(serverAttrPath);
    given(mapAttributeFileLoaderService.load(serverAttrPath, coordinates, mapWidth, mapHeight))
        .willReturn(Optional.of(mapAttributeSet));

    // when
    var result =
        mapAttributeProviderService.getAttributes(mapName, coordinates, mapWidth, mapHeight);

    // then
    assertThat(result).isPresent().contains(mapAttributeSet);

    then(dataPathProperties).should().serverAttrFile(mapName);
    then(mapAttributeFileLoaderService)
        .should()
        .load(serverAttrPath, coordinates, mapWidth, mapHeight);
  }
}
