package com.blaj.openmetin.game.infrastructure.service.animation;

import com.blaj.openmetin.game.application.common.animation.AnimationProviderService;
import com.blaj.openmetin.game.application.common.monster.MonsterDefinitionService;
import com.blaj.openmetin.game.domain.enums.animation.AnimationSubType;
import com.blaj.openmetin.game.domain.enums.animation.AnimationType;
import com.blaj.openmetin.game.domain.model.animation.Animation;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnimationProviderServiceImpl implements AnimationProviderService {

  private final AnimationFileLoaderService animationFileLoaderService;
  private final AnimationCacheService animationCacheService;
  private final MonsterDefinitionService monsterDefinitionService;
  private final DataPathProperties dataPathProperties;

  public void loadAnimations() {
    loadCharacterAnimations();
    loadMonsterAnimations();
  }

  public Optional<Animation> getAnimation(
      long entityId, AnimationType animationType, AnimationSubType animationSubType) {
    return animationCacheService.get(entityId, animationType, animationSubType);
  }

  private void loadCharacterAnimations() {
    var characterClasses = new String[] {"warrior", "assassin", "sura", "shaman"};

    for (int i = 0; i < characterClasses.length; i++) {
      var characterClass = characterClasses[i];

      loadCharacterAnimation(i, characterClass, "pc");
      loadCharacterAnimation(i + 4, characterClass, "pc2");
    }
  }

  private void loadCharacterAnimation(int classId, String characterClass, String pcFolder) {
    var basePath =
        dataPathProperties
            .baseDirectory()
            .resolve(pcFolder)
            .resolve(characterClass)
            .resolve("general");

    loadAndCache(
        classId, AnimationType.WALK, AnimationSubType.GENERAL, basePath.resolve("walk.msa"));
    loadAndCache(classId, AnimationType.RUN, AnimationSubType.GENERAL, basePath.resolve("run.msa"));
  }

  private void loadMonsterAnimations() {
    monsterDefinitionService
        .getMonsterDefinitions()
        .forEach(
            monsterDefinition -> {
              var folder = monsterDefinition.getFolder();

              if (folder.isBlank()) {
                return;
              }

              var trimmedFolder = folder.trim();
              var monster1Path =
                  dataPathProperties.baseDirectory().resolve("monster").resolve(trimmedFolder);
              var monster2Path =
                  dataPathProperties.baseDirectory().resolve("monster2").resolve(trimmedFolder);

              if (Files.isDirectory(monster1Path)) {
                loadMonsterAnimation(monsterDefinition.getId(), monster1Path);
              } else if (Files.isDirectory(monster2Path)) {
                loadMonsterAnimation(monsterDefinition.getId(), monster2Path);
              } else {
                log.warn(
                    "Failed to find animation folder of monster {}({}) {}",
                    monsterDefinition.getName(),
                    monsterDefinition.getId(),
                    folder);
              }
            });
  }

  private void loadMonsterAnimation(long monsterId, Path monsterFolder) {
    var motlistPath = monsterFolder.resolve("motlist.txt");

    if (!Files.exists(motlistPath)) {
      log.warn("No motlist.txt in monster folder {}", monsterFolder);
      return;
    }

    try (var reader = Files.newBufferedReader(motlistPath)) {
      String line;
      while ((line = reader.readLine()) != null) {
        var parts = line.split("[\\t ]");

        if (parts.length != 4) {
          continue;
        }

        if (!parts[0].equalsIgnoreCase("general")) {
          continue;
        }

        var action = parts[1].toLowerCase();
        var animationFile = parts[2];

        if (action.equals("run")) {
          loadAndCache(
              monsterId,
              AnimationType.RUN,
              AnimationSubType.GENERAL,
              monsterFolder.resolve(animationFile));
        } else if (action.equals("walk")) {
          loadAndCache(
              monsterId,
              AnimationType.WALK,
              AnimationSubType.GENERAL,
              monsterFolder.resolve(animationFile));
        }
      }
    } catch (IOException e) {
      log.error("Failed to read motlist.txt for monster {} in {}", monsterId, monsterFolder, e);
    }
  }

  private void loadAndCache(
      long entityId, AnimationType animationType, AnimationSubType animationSubType, Path path) {
    animationFileLoaderService
        .loadAnimation(path)
        .ifPresentOrElse(
            animation -> {
              animationCacheService.put(entityId, animationType, animationSubType, animation);
              log.debug(
                  "Loaded animation: {} {} {} from {}",
                  entityId,
                  animationType,
                  animationSubType,
                  path.getFileName());
            },
            () -> log.warn("Failed to load animation from {}", path));
  }
}
