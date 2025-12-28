package com.blaj.openmetin.game.domain.enums.map;

import java.util.EnumSet;
import java.util.Set;

public enum MapAttribute {
  NONE(0),
  BLOCK(1 << 0),
  WATER(1 << 1),
  WARP(1 << 2),
  SAFE(1 << 3),
  EMPIRE(1 << 4),
  MONSTERAREA(1 << 5),
  NO_MOUNT(1 << 6);

  private final int value;

  MapAttribute(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static boolean hasFlag(int attributes, MapAttribute flag) {
    return (attributes & flag.value) != 0;
  }

  public static Set<MapAttribute> fromInt(int value) {
    var flags = EnumSet.noneOf(MapAttribute.class);

    for (var flag : values()) {
      if (flag != NONE && (value & flag.value) != 0) {
        flags.add(flag);
      }
    }

    return flags;
  }

  public static int combine(MapAttribute... flags) {
    var result = 0;

    for (var flag : flags) {
      result |= flag.value;
    }

    return result;
  }
}
