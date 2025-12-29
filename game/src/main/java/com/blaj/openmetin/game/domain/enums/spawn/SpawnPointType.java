package com.blaj.openmetin.game.domain.enums.spawn;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum SpawnPointType {
  GROUP('g'),
  MONSTER('m'),
  EXCEPTION('e'),
  GROUP_COLLECTION('r'),
  SPECIAL('s');

  private final char code;

  SpawnPointType(char code) {
    this.code = code;
  }

  public static SpawnPointType fromCode(char code) {
    return Arrays.stream(values())
        .filter(spawnPointType -> spawnPointType.code == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown spawn point type code: " + code));
  }
}
