package com.blaj.openmetin.game.infrastructure.service.animation;

import com.blaj.openmetin.game.domain.enums.animation.AnimationSubType;
import com.blaj.openmetin.game.domain.enums.animation.AnimationType;
import com.blaj.openmetin.game.domain.model.animation.Animation;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AnimationCacheService {

  private final Map<Long, Map<AnimationType, Map<AnimationSubType, Animation>>> animations =
      new ConcurrentHashMap<>();

  public void put(
      long entityId,
      AnimationType animationType,
      AnimationSubType animationSubType,
      Animation animation) {
    animations
        .computeIfAbsent(entityId, k -> new ConcurrentHashMap<>())
        .computeIfAbsent(animationType, k -> new ConcurrentHashMap<>())
        .put(animationSubType, animation);
  }

  public Optional<Animation> get(
      long entityId, AnimationType animationType, AnimationSubType animationSubType) {
    return Optional.ofNullable(animations.get(entityId))
        .flatMap(typeMap -> Optional.ofNullable(typeMap.get(animationType)))
        .flatMap(subTypeMap -> Optional.ofNullable(subTypeMap.get(animationSubType)));
  }

  public void clear() {
    animations.clear();
  }

  public int size() {
    return animations.values().stream()
        .mapToInt(typeMap -> typeMap.values().stream().mapToInt(Map::size).sum())
        .sum();
  }
}
