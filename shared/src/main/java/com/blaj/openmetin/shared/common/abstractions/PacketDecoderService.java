package com.blaj.openmetin.shared.common.abstractions;

import com.blaj.openmetin.contracts.enums.PacketDirection;
import com.blaj.openmetin.shared.common.model.Packet;
import io.netty.buffer.ByteBuf;
import java.util.Set;

public interface PacketDecoderService<T extends Packet> {
  int getHeader();

  Set<PacketDirection> getDirection();

  T decode(ByteBuf byteBuf);
}
