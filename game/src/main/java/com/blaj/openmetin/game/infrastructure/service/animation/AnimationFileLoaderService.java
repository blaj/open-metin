package com.blaj.openmetin.game.infrastructure.service.animation;

import com.blaj.openmetin.game.domain.model.animation.Animation;
import com.blaj.openmetin.game.infrastructure.exception.AnimationParseException;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnimationFileLoaderService {

  private final DataPathProperties dataPathProperties;

  public Optional<Animation> loadAnimation(Path animationPath) {
    if (!Files.exists(animationPath)) {
      log.warn("Animation file not found: {}", animationPath);

      return Optional.empty();
    }

    return parseFile(animationPath);
  }

  private Optional<Animation> parseFile(Path animationPath) {
    var properties = new HashMap<String, String>();

    try (var lines = Files.lines(animationPath, StandardCharsets.UTF_8)) {
      lines
          .map(String::trim)
          .filter(line -> !line.isEmpty())
          .filter(line -> !line.startsWith("#"))
          .forEach(line -> parseLine(line, properties));

      return buildAnimationFromProperties(properties, animationPath);
    } catch (IOException e) {
      log.error("Failed to load animation from {}", animationPath, e);
    }

    return Optional.empty();
  }

  private void parseLine(String line, Map<String, String> properties) {
    var parts = line.split("\\s+", 2);

    if (parts.length < 2) {
      return;
    }

    var key = parts[0].trim();
    var value = parts[1].trim();

    properties.put(key, value);
  }

  private Optional<Animation> buildAnimationFromProperties(
      Map<String, String> properties, Path animationPath) {
    var scriptType = properties.get("ScriptType");

    if (!scriptType.equals("MotionData")) {
      throw new AnimationParseException(
          "Invalid msa file %s. Expected ScriptType 'MotionData' but got '%s'"
              .formatted(animationPath, scriptType));
    }

    var duration = parseFloat(properties.get("MotionDuration"), "MotionDuration", animationPath);
    var accumulationParts =
        Optional.ofNullable(properties.get("Accumulation"))
            .map(accumulation -> accumulation.split(("\\s+")))
            .filter(parts -> parts.length >= 3)
            .orElseThrow(
                () ->
                    new AnimationParseException(
                        "Missing Accumulation in %s".formatted(animationPath)));

    var accumulationX = parseFloat(accumulationParts[0], "Accumulation X", animationPath);
    var accumulationY = parseFloat(accumulationParts[1], "Accumulation Y", animationPath);
    var accumulationZ = parseFloat(accumulationParts[2], "Accumulation Z", animationPath);

    return Optional.of(new Animation(duration, accumulationX, accumulationY, accumulationZ));
  }

  private float parseFloat(String value, String fieldName, Path animationPath) {
    return Optional.ofNullable(value)
        .map(Float::parseFloat)
        .orElseThrow(
            () ->
                new AnimationParseException(
                    "Missing %s in %s".formatted(fieldName, animationPath)));
  }
}
