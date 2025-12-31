package com.blaj.openmetin.game.domain.enums.character;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import lombok.Getter;

@Getter
public enum Empire implements ByteEnum {
  NEUTRAL((byte) 0),
  SHINSOO((byte) 1),
  CHUNJO((byte) 2),
  JINNO((byte) 3);

  private final byte value;

  Empire(byte value) {
    this.value = value;
  }
}
