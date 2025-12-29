package com.blaj.openmetin.shared.common.enums;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import lombok.Getter;

@Getter
public enum Phase implements ByteEnum {
  HANDSHAKE((byte) 1),
  LOGIN((byte) 2),
  SELECT_CHARACTER((byte) 3),
  LOADING((byte) 4),
  IN_GAME((byte) 5),
  AUTH((byte) 10);

  private final byte value;

  Phase(byte value) {
    this.value = value;
  }
}
