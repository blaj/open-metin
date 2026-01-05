package com.blaj.openmetin.game.domain.enums.common;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import lombok.Getter;

@Getter
public enum ImmuneType implements ByteEnum {
  STUN((byte) (1 << 0)),
  SLOW((byte) (1 << 1)),
  FALL((byte) (1 << 2)),
  CURSE((byte) (1 << 3)),
  POISON((byte) (1 << 4)),
  TERROR((byte) (1 << 5)),
  REFLECT((byte) (1 << 6));

  private final byte value;

  ImmuneType(byte value) {
    this.value = value;
  }
}
