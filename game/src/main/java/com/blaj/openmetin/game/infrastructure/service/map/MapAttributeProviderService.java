package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.domain.model.map.MapAttributeSet;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import com.blaj.openmetin.shared.domain.model.Coordinates;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
