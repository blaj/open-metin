package com.blaj.openmetin.authentication.application.features.login;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0x96, direction = PacketDirection.OUTGOING)
@Getter
@Setter
@Accessors(chain = true)
public class LoginSuccessPacket implements Packet {

  @PacketField(position = 0, unsigned = true)
  private long key;

  @PacketField(position = 1)
  private byte result;
}
