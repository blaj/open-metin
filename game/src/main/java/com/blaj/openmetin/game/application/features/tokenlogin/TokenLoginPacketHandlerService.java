package com.blaj.openmetin.game.application.features.tokenlogin;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenLoginPacketHandlerService implements PacketHandlerService<TokenLoginPacket> {

  private final Mediator mediator;

  @Override
  public void handle(TokenLoginPacket packet, Session session) {
    mediator.sendAsync(
        new TokenLoginCommand(
            packet.getUsername(), packet.getKey(), packet.getEncryptKeys(), session.getId()));
  }

  @Override
  public Class<TokenLoginPacket> getPacketType() {
    return TokenLoginPacket.class;
  }
}
