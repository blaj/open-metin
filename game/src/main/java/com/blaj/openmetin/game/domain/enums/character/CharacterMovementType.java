package com.blaj.openmetin.game.domain.enums.character;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import lombok.Getter;

@Getter
public enum CharacterMovementType implements ByteEnum {
  WAIT((byte) 0),
  MOVE((byte) 1),
  ATTACK((byte) 2),
  COMBO((byte) 3),
  MOB_SKILL((byte) 4),
  MAX((byte) 6),
  SKILL((byte) 0x80);

  private final byte value;

  CharacterMovementType(byte value) {
    this.value = value;
  }
}
