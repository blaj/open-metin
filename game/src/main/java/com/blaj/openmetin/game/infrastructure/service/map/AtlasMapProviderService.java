package com.blaj.openmetin.game.infrastructure.service.map;

import com.blaj.openmetin.game.domain.model.map.Coordinates;
import com.blaj.openmetin.game.domain.model.map.Map;
import com.blaj.openmetin.game.domain.model.map.TownCoordinates;
import com.blaj.openmetin.game.infrastructure.exception.AtlasInfoLoadException;
import com.blaj.openmetin.game.infrastructure.exception.AtlasInfoParseException;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtlasMapProviderService {

  private static final Pattern LINE_PATTERN =
      Pattern.compile("^([a-zA-Z0-9/_]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)$");

  private final DataPathProperties dataPathProperties;

  public List<Map> getAll() {
    var atlasInfoPath = dataPathProperties.atlasInfoFile();

    if (!Files.exists(atlasInfoPath)) {
      log.warn("atlasinfo.txt not found at {}.", atlasInfoPath.toAbsolutePath());

      return Collections.emptyList();
    }

    return parseAtlasInfo(atlasInfoPath).stream()
        .map(
            atlasInfo ->
                new Map(
                    atlasInfo.name(),
                    atlasInfo.coordinates(),
                    atlasInfo.width(),
                    atlasInfo.height(),
                    getTownCoordinates(atlasInfo.name).orElse(null)))
        .toList();
  }

  public Optional<TownCoordinates> getTownCoordinates(String mapName) {
    var townPath = dataPathProperties.townFile(mapName);

    if (!Files.exists(townPath)) {
      log.warn("Town.txt not found for map {} at {}", mapName, townPath.toAbsolutePath());

      return Optional.empty();
    }

    return parseTownCoordinates(townPath, mapName);
  }

  private List<AtlasInfo> parseAtlasInfo(Path atlasInfoPath) {
    try (var lines = Files.lines(atlasInfoPath, StandardCharsets.UTF_8)) {
      var lineCounter = new AtomicInteger(0);

      return lines
          .map(String::trim)
          .map(line -> new LineWrapper(lineCounter.getAndIncrement(), line))
          .filter(lineWrapper -> !lineWrapper.line().isEmpty())
          .filter(lineWrapper -> !lineWrapper.line().startsWith("#"))
          .map(this::parseAtlasInfoLine)
          .toList();
    } catch (IOException e) {
      log.error("Failed to read atlasinfo.txt from {}", atlasInfoPath.toAbsolutePath(), e);

      throw new AtlasInfoLoadException("Failed to load atlas info", e);
    }
  }

  private AtlasInfo parseAtlasInfoLine(LineWrapper lineWrapper) {
    var matcher = LINE_PATTERN.matcher(lineWrapper.line());

    if (!matcher.matches()) {
      throw new AtlasInfoParseException(
          "Failed to parse atlasinfo.txt:line %d - Invalid format".formatted(lineWrapper.lineNo()));
    }

    var mapName = matcher.group(1);
    var positionX = Integer.parseInt(matcher.group(2));
    var positionY = Integer.parseInt(matcher.group(3));
    var width = Integer.parseInt(matcher.group(4));
    var height = Integer.parseInt(matcher.group(5));

    return new AtlasInfo(mapName, new Coordinates(positionX, positionY), width, height);
  }

  private Optional<TownCoordinates> parseTownCoordinates(Path townPath, String mapName) {
    try (var lines = Files.lines(townPath, StandardCharsets.UTF_8)) {
      var lineCounter = new AtomicInteger(0);

      var coordinates =
          lines
              .map(String::trim)
              .map(line -> new LineWrapper(lineCounter.getAndIncrement(), line))
              .filter(lineWrapper -> !lineWrapper.line.isEmpty())
              .filter(lineWrapper -> !lineWrapper.line().startsWith("#"))
              .map(lineWrapper -> parseTownLine(lineWrapper, mapName))
              .flatMap(Optional::stream)
              .toList();

      return switch (coordinates.size()) {
        case 1 -> Optional.of(TownCoordinates.allOf(coordinates.getFirst()));
        case 4 ->
            Optional.of(
                new TownCoordinates(
                    coordinates.get(0),
                    coordinates.get(1),
                    coordinates.get(2),
                    coordinates.get(3)));
        default ->
            throw new AtlasInfoParseException(
                "Town.txt for map %s must contain 1 or 4 coordinate lines, found %d"
                    .formatted(mapName, coordinates.size()));
      };
    } catch (IOException e) {
      log.error(
          "Failed to read Town.txt for map {} from {}", mapName, townPath.toAbsolutePath(), e);
    }

    return Optional.empty();
  }

  private Optional<Coordinates> parseTownLine(LineWrapper lineWrapper, String mapName) {
    var parts = lineWrapper.line().split(("\\s+"));

    if (parts.length < 2) {
      log.warn("Invalid line {} in Town.txt for map {}", lineWrapper.lineNo(), mapName);

      return Optional.empty();
    }

    var x = Integer.parseInt(parts[0]);
    var y = Integer.parseInt(parts[1]);

    return Optional.of(new Coordinates(x, y));
  }

  private record LineWrapper(int lineNo, String line) {}

  private record AtlasInfo(String name, Coordinates coordinates, int width, int height) {}
}
