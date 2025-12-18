package com.blaj.openmetin.game.application.features.marklogin;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import org.springframework.stereotype.Service;

@Service
public class MarkLoginPacketHandlerService implements PacketHandlerService<MarkLoginPacket> {

  @Override
  public void handle(MarkLoginPacket packet, Session session) {}

  @Override
  public Class<MarkLoginPacket> getPacketType() {
    return MarkLoginPacket.class;
  }
}
