package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.domain.model.map.Coordinates;
import com.blaj.openmetin.game.domain.model.map.MapAttributeSet;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapAttributeProviderService {

  private final DataPathProperties dataPathProperties;
  private final MapAttributeFileLoaderService mapAttributeFileLoaderService;

  public Optional<MapAttributeSet> getAttributes(
      String mapName, Coordinates coordinates, long mapWidth, long mapHeight) {
    return mapAttributeFileLoaderService.load(
        dataPathProperties.serverAttrFile(mapName), coordinates, mapWidth, mapHeight);
  }
}
