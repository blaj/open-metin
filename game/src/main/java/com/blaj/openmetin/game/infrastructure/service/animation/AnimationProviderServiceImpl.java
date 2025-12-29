package com.blaj.openmetin.game.infrastructure.service.animation;

import com.blaj.openmetin.game.application.common.animation.AnimationProviderService;
import com.blaj.openmetin.game.domain.enums.animation.AnimationSubType;
import com.blaj.openmetin.game.domain.enums.animation.AnimationType;
import com.blaj.openmetin.game.domain.model.animation.Animation;
import com.blaj.openmetin.game.infrastructure.properties.DataPathProperties;
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
  private final DataPathProperties dataPathProperties;

  public void loadAnimations() {
    loadCharacterAnimations();
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
