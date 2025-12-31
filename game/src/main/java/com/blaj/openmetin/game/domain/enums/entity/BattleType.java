package com.blaj.openmetin.game.domain.enums.entity;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import lombok.Getter;

@Getter
public enum BattleType implements ByteEnum {
  MELEE((byte) 0),
  RANGE((byte) 1),
  MAGIC((byte) 2),
  SPECIAL((byte) 3),
  POWER((byte) 4),
  TANKER((byte) 5),
  SUPER_POWER((byte) 6),
  SUPER_TANKER((byte) 7);

  private final byte value;

  BattleType(byte value) {
    this.value = value;
  }
}
