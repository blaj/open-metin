package com.blaj.openmetin.game.domain.enums.spawn;

import com.blaj.openmetin.contracts.enums.ByteEnum;

public enum SpawnPointDirection implements ByteEnum {
  RANDOM((byte) 0),
  SOUTH((byte) 1),
  SOUTH_EAST((byte) 2),
  EAST((byte) 3),
  NORTH_EAST((byte) 4),
  NORTH((byte) 5),
  NORTH_WEST((byte) 6),
  WEST((byte) 7),
  SOUTH_WEST((byte) 8);

  private final byte value;

  SpawnPointDirection(byte value) {
    this.value = value;
  }

  public byte getValue() {
    return value;
  }
}
