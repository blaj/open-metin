package com.blaj.openmetin.shared.application.features.handshake;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HandshakePacketHandlerService implements PacketHandlerService<HandshakePacket> {

  private final Mediator mediator;

  @Override
  public void handle(HandshakePacket packet, Session session) {
    mediator.send(
        new HandshakeCommand(
            packet.getHandshake(), packet.getTime(), packet.getDelta(), session.getId()));
  }

  @Override
  public Class<HandshakePacket> getPacketType() {
    return HandshakePacket.class;
  }
}
