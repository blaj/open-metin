package com.blaj.openmetin.shared.common.abstractions;

import com.blaj.openmetin.shared.common.model.Packet;
import com.blaj.openmetin.shared.common.model.Session;

public interface PacketHandlerService<T extends Packet> {
  void handle(T packet, Session session);

  Class<T> getPacketType();
}
