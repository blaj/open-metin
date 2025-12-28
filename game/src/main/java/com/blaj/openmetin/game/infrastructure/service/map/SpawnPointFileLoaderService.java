package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.domain.enums.spawn.SpawnPointType;
import com.blaj.openmetin.game.domain.model.spawn.SpawnPoint;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
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

    var spawnPoint = new SpawnPoint();
    parseTypeAndAggressive(spawnPoint, parts[0]);

    spawnPoint.setX(Integer.parseInt(parts[1]));
    spawnPoint.setY(Integer.parseInt(parts[2]));
    spawnPoint.setRangeX(Integer.parseInt(parts[3]));
    spawnPoint.setRangeY(Integer.parseInt(parts[4]));

    return Optional.of(spawnPoint);
  }

  private void parseTypeAndAggressive(SpawnPoint spawnPoint, String typeField){
    if (typeField.isEmpty()) {
      throw new IllegalArgumentException("Empty type field");
    }

    var typeChar = typeField.charAt(0);
    spawnPoint.setType(SpawnPointType.fromCode(typeChar));

    if (typeField.length() > 1) {
      var secondChar = typeField.charAt(1);
      spawnPoint.setAggressive(secondChar == 'a' || secondChar == 'A');
    } else {
      spawnPoint.setAggressive(false);
    }
  }
}
