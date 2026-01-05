package com.blaj.openmetin.game.domain.enums.spawn;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import java.util.Arrays;
import lombok.Getter;

@Getter
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

  public static SpawnPointDirection fromValue(int value) {
    return Arrays.stream(values())
        .filter(spawnPointDirection -> spawnPointDirection.value == value)
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unknown spawn point direction value: " + value));
  }
}
