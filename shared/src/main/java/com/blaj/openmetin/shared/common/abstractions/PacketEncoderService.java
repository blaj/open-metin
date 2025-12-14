package com.blaj.openmetin.shared.common.abstractions;

import com.blaj.openmetin.shared.common.model.Packet;
import io.netty.buffer.ByteBuf;

public interface PacketEncoderService<T extends Packet> {
  int getHeader();

  Class<T> getPacketClass();

  void encode(T packet, ByteBuf byteBuf);
}
