package com.blaj.openmetin.game.domain.entity;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joou.UByte;
import org.joou.UShort;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ServerStatus implements Serializable {

  private UByte channelIndex;

  private UShort port;

  private Status status;

  public enum Status implements ByteEnum {
    UNKNOWN((byte) 0),
    NORMAL((byte) 1),
    BUSY((byte) 2),
    FULL((byte) 3);

    private final byte value;

    Status(byte value) {
      this.value = value;
    }

    @Override
    public byte getValue() {
      return value;
    }
  }
}
