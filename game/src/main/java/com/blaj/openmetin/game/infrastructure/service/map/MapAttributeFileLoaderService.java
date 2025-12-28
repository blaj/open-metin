package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.domain.enums.map.MapAttribute;
import com.blaj.openmetin.game.domain.model.map.Coordinates;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.map.MapAttributeSectree;
import com.blaj.openmetin.game.domain.model.map.MapAttributeSet;
import com.blaj.openmetin.game.infrastructure.service.compression.LzoDecompressorService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
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
      var attributeSet = loadFromFile(serverAttrPath, basePosition, mapWidth, mapHeight);
      return Optional.of(attributeSet);
    } catch (IOException e) {
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

    // Load sectrees
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
      Path path, ByteBuffer buffer, int sectreesWidth, int sectreesHeight) throws IOException {

    var sectrees = new MapAttributeSectree[sectreesHeight][sectreesWidth];

    var debugInfo = log.isDebugEnabled() ? new DebugInfo() : null;

    for (int y = 0; y < sectreesHeight; y++) {
      for (int x = 0; x < sectreesWidth; x++) {
        var blockSize = buffer.getInt();
        var compressedData = new byte[blockSize];
        buffer.get(compressedData);

        sectrees[y][x] = loadSectree(path, x, y, compressedData, debugInfo);
      }
    }

    if (debugInfo != null) {
      logDebugInfo(path, sectreesHeight, sectreesWidth, debugInfo);
    }

    return sectrees;
  }

  private MapAttributeSectree loadSectree(
      Path path, int x, int y, byte[] compressedData, DebugInfo debugInfo) {

    try {
      var decompressedData =
          lzoDecompressorService.decompress(
              MapAttributeSet.CELLS_PER_SECTREE * Integer.BYTES, compressedData);
      return parseSectree(decompressedData, debugInfo);
    } catch (Exception e) {
      log.warn("{} failed to decode sectree ({}, {}): {}", path, x, y, e.getMessage());
      return MapAttributeSectree.EMPTY;
    }
  }

  private MapAttributeSectree parseSectree(byte[] data, DebugInfo debugInfo) {
    if (data.length != MapAttributeSet.CELLS_PER_SECTREE * Integer.BYTES) {
      throw new IllegalArgumentException(
          "Unexpected sectree size: expected %d, got %d"
              .formatted(MapAttributeSet.CELLS_PER_SECTREE * Integer.BYTES, data.length));
    }

    var buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    var cellAttributes = new int[MapAttributeSet.CELLS_PER_SECTREE];

    for (int i = 0; i < cellAttributes.length; i++) {
      var rawCellFlags = buffer.getInt();
      cellAttributes[i] = rawCellFlags;

      if (debugInfo != null) {
        debugInfo.recordCell(rawCellFlags);
      }
    }

    return new MapAttributeSectree(cellAttributes);
  }

  private void logDebugInfo(Path path, int height, int width, DebugInfo debugInfo) {
    var totalCells = (long) height * width * MapAttributeSet.CELLS_PER_SECTREE;
    var percentage = 100.0 * debugInfo.cellsWithKnownFlags / totalCells;

    log.debug(
        "Loaded {} {}x{} cell attributes: {} (cells with known flags: {}/{} = {:.2f}%)",
        path,
        MapAttributeSet.CELL_SIZE,
        MapAttributeSet.CELL_SIZE,
        debugInfo.getFlagSummary(),
        debugInfo.cellsWithKnownFlags,
        totalCells,
        percentage);

    if (!debugInfo.unknownFlagCounts.isEmpty()) {
      log.warn(
          "{} contains cells with unknown flag bits (LSB being bit 0): {}",
          path,
          debugInfo.getUnknownFlagsSummary());
    }
  }

  private static class DebugInfo {

    long cellsWithKnownFlags = 0;
    final java.util.Map<MapAttribute, Long> flagCounts = new EnumMap<>(MapAttribute.class);
    final java.util.Map<Integer, Long> unknownFlagCounts = new HashMap<>();

    DebugInfo() {
      for (var flag : MapAttribute.values()) {
        if (flag != MapAttribute.NONE) {
          flagCounts.put(flag, 0L);
        }
      }
    }

    void recordCell(int rawFlags) {
      boolean cellHasValidFlag = false;

      // Count known flags
      for (var flag : flagCounts.keySet()) {
        if (MapAttribute.hasFlag(rawFlags, flag)) {
          flagCounts.put(flag, flagCounts.get(flag) + 1);
          cellHasValidFlag = true;
        }
      }

      if (cellHasValidFlag) {
        cellsWithKnownFlags++;
      }

      // Find unknown flags
      int knownFlagsMask = MapAttribute.combine(flagCounts.keySet().toArray(new MapAttribute[0]));
      int unknownFlags = rawFlags & ~knownFlagsMask;

      if (unknownFlags != 0) {
        int remaining = unknownFlags;
        while (remaining != 0) {
          int lsb = remaining & ~(remaining - 1);
          int bitPosition = Integer.numberOfTrailingZeros(lsb);
          unknownFlagCounts.merge(bitPosition, 1L, Long::sum);
          remaining &= remaining - 1;
        }
      }
    }

    String getFlagSummary() {
      return flagCounts.entrySet().stream()
          .map(e -> e.getKey() + "=" + e.getValue())
          .collect(java.util.stream.Collectors.joining(" "));
    }

    String getUnknownFlagsSummary() {
      return unknownFlagCounts.entrySet().stream()
          .map(e -> "bit " + e.getKey() + " found in " + e.getValue() + " cells")
          .collect(java.util.stream.Collectors.joining(", "));
    }
  }
}
