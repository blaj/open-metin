package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointDirection;
import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointType;
import com.blaj.openmetin.game.domain.model.spawn.SpawnPoint;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpawnPointFileLoaderService {

  private final DataPathProperties dataPathProperties;

  public List<SpawnPoint> load(String mapName) {
    var spawnPoints = new ArrayList<SpawnPoint>();

    parseFile(mapName, "regen.txt", spawnPoints);
    parseFile(mapName, "npc.txt", spawnPoints);
    parseFile(mapName, "stone.txt", spawnPoints);
    parseFile(mapName, "boss.txt", spawnPoints);

    return spawnPoints;
  }

  private void parseFile(String mapName, String fileName, List<SpawnPoint> spawnPoints) {
    var filePath = dataPathProperties.mapsDirectory().resolve(mapName).resolve(fileName);

    if (!Files.exists(filePath)) {
      log.warn("Spawn file not found: {}", filePath);
      return;
    }

    try (var lines = Files.lines(filePath, StandardCharsets.ISO_8859_1)) {
      lines
          .map(String::trim)
          .filter(line -> !line.isEmpty())
          .filter(line -> !line.startsWith("#"))
          .filter(line -> !line.startsWith("//"))
          .map(this::parseLine)
          .flatMap(Optional::stream)
          .forEach(spawnPoints::add);
    } catch (IOException e) {
      log.error("Failed to load spawn points from {}", filePath, e);
    }
  }

  private Optional<SpawnPoint> parseLine(String line) {
    var parts = line.split("\\s+");

    if (parts.length < 11) {
      log.warn("Invalid spawn point line (not enough fields): {}", line);

      return Optional.empty();
    }

    var spawnPointTypeWrapper = parseTypeAndAggressive(parts[0]);

    var spawnPoint =
        new SpawnPoint(
            spawnPointTypeWrapper.spawnPointType(),
            spawnPointTypeWrapper.isAggressive(),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]),
            Integer.parseInt(parts[3]),
            Integer.parseInt(parts[4]),
            SpawnPointDirection.fromValue(Integer.parseInt(parts[6])),
            0,
            Collections.emptyList(),
            Long.parseLong(parts[10]),
            null,
            Short.parseShort(parts[8]),
            Short.parseShort(parts[9]));

    return Optional.of(spawnPoint);
  }

  private SpawnPointTypeWrapper parseTypeAndAggressive(String typeField) {
    if (typeField.isEmpty()) {
      throw new IllegalArgumentException("Empty type field");
    }

    var isAggressive = false;
    if (typeField.length() > 1) {
      var secondChar = typeField.charAt(1);
      isAggressive = secondChar == 'a' || secondChar == 'A';
    }

    return new SpawnPointTypeWrapper(SpawnPointType.fromCode(typeField.charAt(0)), isAggressive);
  }

  private int parseRespawnTime(String field) {
    return -1; // TODO
  }

  private record SpawnPointTypeWrapper(SpawnPointType spawnPointType, boolean isAggressive) {}
}
