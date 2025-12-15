package com.blaj.openmetin.shared.application.features.handshake;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@GeneratePacketCodec
@PacketHeader(header = 0xff, direction = PacketDirection.INCOMING)
@Getter
@Setter
@Accessors(chain = true)
public class HandshakePacket implements Packet {

  @PacketField(position = 0, unsigned = true)
  private long handshake;

  @PacketField(position = 1, unsigned = true)
  private long time;

  @PacketField(position = 2)
  private int delta;
}
