package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.map.MapAttributeSectree;
import com.blaj.openmetin.game.domain.model.map.MapAttributeSet;
import com.blaj.openmetin.game.infrastructure.service.compression.LzoDecompressorService;
import com.blaj.openmetin.shared.domain.model.Coordinates;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapAttributeFileLoaderService {

  private final LzoDecompressorService lzoDecompressorService;

  public Optional<MapAttributeSet> load(
      Path serverAttrPath, Coordinates basePosition, long mapWidth, long mapHeight) {

    if (!Files.exists(serverAttrPath)) {
      log.debug("No server_attr file found at {}", serverAttrPath);
      return Optional.empty();
    }

    try {
      return Optional.of(loadFromFile(serverAttrPath, basePosition, mapWidth, mapHeight));
    } catch (IOException | BufferUnderflowException e) {
      log.error("Failed to load map attributes from {}", serverAttrPath, e);
      return Optional.empty();
    }
  }

  private MapAttributeSet loadFromFile(
      Path serverAttrPath, Coordinates basePosition, long mapWidth, long mapHeight)
      throws IOException {

    log.debug("Loading cell attributes from file {}", serverAttrPath);

    var bytes = Files.readAllBytes(serverAttrPath);
    var buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

    var sectreesWidth = buffer.getInt();
    var sectreesHeight = buffer.getInt();

    validateDimensions(serverAttrPath, sectreesWidth, sectreesHeight, mapWidth, mapHeight);

    var sectrees = loadSectrees(serverAttrPath, buffer, sectreesWidth, sectreesHeight);

    return new MapAttributeSet(sectreesWidth, sectreesHeight, basePosition, sectrees);
  }

  private void validateDimensions(
      Path path, int sectreesWidth, int sectreesHeight, long mapWidth, long mapHeight) {

    var expectedSectreesWidth = (int) (mapWidth * Map.MAP_UNIT / MapAttributeSet.SECTREE_SIZE);
    var expectedSectreesHeight = (int) (mapHeight * Map.MAP_UNIT / MapAttributeSet.SECTREE_SIZE);

    if (sectreesWidth != expectedSectreesWidth || sectreesHeight != expectedSectreesHeight) {
      log.warn(
          "{} dimensions ({}x{}) do not match atlasinfo.txt dimensions ({}x{})",
          path,
          sectreesWidth,
          sectreesHeight,
          expectedSectreesWidth,
          expectedSectreesHeight);
    }
  }

  private MapAttributeSectree[][] loadSectrees(
      Path path, ByteBuffer buffer, int sectreesWidth, int sectreesHeight) {

    var sectrees = new MapAttributeSectree[sectreesHeight][sectreesWidth];

    for (var y = 0; y < sectreesHeight; y++) {
      for (var x = 0; x < sectreesWidth; x++) {
        var blockSize = buffer.getInt();
        var compressedData = new byte[blockSize];
        buffer.get(compressedData);

        sectrees[y][x] = loadSectree(path, x, y, compressedData);
      }
    }

    return sectrees;
  }

  private MapAttributeSectree loadSectree(Path path, int x, int y, byte[] compressedData) {
    try {
      var decompressedData =
          lzoDecompressorService.decompress(
              MapAttributeSet.CELLS_PER_SECTREE * Integer.BYTES, compressedData);
      return parseSectree(decompressedData);
    } catch (Exception e) {
      log.warn("{} failed to decode sectree ({}, {}): {}", path, x, y, e.getMessage());

      return MapAttributeSectree.EMPTY;
    }
  }

  private MapAttributeSectree parseSectree(byte[] data) {
    if (data.length != MapAttributeSet.CELLS_PER_SECTREE * Integer.BYTES) {
      throw new IllegalArgumentException(
          "Unexpected sectree size: expected %d, got %d"
              .formatted(MapAttributeSet.CELLS_PER_SECTREE * Integer.BYTES, data.length));
    }

    var buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    var cellAttributes = new int[MapAttributeSet.CELLS_PER_SECTREE];

    for (var i = 0; i < cellAttributes.length; i++) {
      var rawCellFlags = buffer.getInt();
      cellAttributes[i] = rawCellFlags;
    }

    return new MapAttributeSectree(cellAttributes);
  }
}
