package com.blaj.openmetin.game.domain.enums.character;

import com.blaj.openmetin.contracts.enums.ByteEnum;

public enum CharacterMovementType implements ByteEnum {
  WAIT((byte) 0),
  MOVE((byte) 1),
  ATTACK((byte) 2),
  COMBO((byte) 3),
  MOB_SKILL((byte) 4);

  public static final byte SKILL_FLAG = (byte) (1 << 7);

  private final byte value;

  CharacterMovementType(byte value) {
    this.value = value;
  }

  @Override
  public byte getValue() {
    return value;
  }

  public boolean hasSkillFlag() {
    return (value & SKILL_FLAG) != 0;
  }
}
