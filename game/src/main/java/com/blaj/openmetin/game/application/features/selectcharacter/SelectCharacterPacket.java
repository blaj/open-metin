package com.blaj.openmetin.game.application.features.selectcharacter;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0x06, direction = PacketDirection.INCOMING, isSequence = true)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class SelectCharacterPacket implements Packet {

  @PacketField(position = 0, unsigned = true)
  private short slot;
}
