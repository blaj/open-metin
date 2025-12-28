package com.blaj.openmetin.game.domain.enums.spawn;

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

  public char getCode() {
    return code;
  }

  public static SpawnPointType fromCode(char code) {
    for (var type : values()) {
      if (type.code == code) {
        return type;
      }
    }

    throw new IllegalArgumentException("Unknown spawn point type code: " + code);
  }

  public static SpawnPointType fromCode(String code) {
    if (code == null || code.isEmpty()) {
      throw new IllegalArgumentException("Spawn point type code cannot be empty");
    }

    return fromCode(code.charAt(0));
  }
}
