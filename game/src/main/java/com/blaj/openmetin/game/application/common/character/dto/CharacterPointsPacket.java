package com.blaj.openmetin.game.application.common.character.dto;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.game.shared.constants.CharacterConstants;
import com.blaj.openmetin.shared.common.model.Packet;
import java.util.Arrays;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0x10, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class CharacterPointsPacket implements Packet {

  @PacketField(
      position = 0,
      unsigned = true,
      arrayLength = CharacterConstants.CHARACTER_POINTS_COUNT)
  private long[] points = new long[CharacterConstants.CHARACTER_POINTS_COUNT];

  {
    Arrays.fill(points, 0);
  }
}
