package com.blaj.openmetin.game.application.features.marklogin;

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
@PacketHeader(header = 0x64, direction = PacketDirection.INCOMING)
@Getter
@Setter
@Accessors(chain = true)
public class MarkLoginPacket implements Packet {

  @PacketField(position = 0)
  private UInteger handle;

  @PacketField(position = 1)
  private UInteger randomKey;
}
