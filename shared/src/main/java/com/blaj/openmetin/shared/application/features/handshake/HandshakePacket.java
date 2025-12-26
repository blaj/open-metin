package com.blaj.openmetin.shared.application.features.handshake;

import com.blaj.openmetin.contracts.annotation.GeneratePacketCodec;
import com.blaj.openmetin.contracts.annotation.PacketField;
import com.blaj.openmetin.contracts.annotation.PacketHeader;
import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.model.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joou.UInteger;

@GeneratePacketCodec
@PacketHeader(
    header = 0xff,
    direction = {PacketDirection.INCOMING, PacketDirection.OUTGOING})
@Getter
@Setter
@Accessors(chain = true)
public class HandshakePacket implements Packet {

  @PacketField(position = 0)
  private UInteger handshake;

  @PacketField(position = 1)
  private UInteger time;

  @PacketField(position = 2)
  private int delta;
}
