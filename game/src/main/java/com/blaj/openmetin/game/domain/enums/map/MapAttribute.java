package com.blaj.openmetin.game.domain.enums.map;

import lombok.Getter;

@Getter
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

  public static boolean hasFlag(int attributes, MapAttribute flag) {
    return (attributes & flag.value) != 0;
  }
}
