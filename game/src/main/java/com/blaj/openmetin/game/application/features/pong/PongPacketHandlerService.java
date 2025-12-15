package com.blaj.openmetin.game.application.features.pong;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import org.springframework.stereotype.Service;

@Service
public class PongPacketHandlerService implements PacketHandlerService<PongPacket> {

  @Override
  public void handle(PongPacket packet, Session session) {}

  @Override
  public Class<PongPacket> getPacketType() {
    return PongPacket.class;
  }
}
