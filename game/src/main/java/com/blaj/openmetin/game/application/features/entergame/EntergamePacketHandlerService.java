package com.blaj.openmetin.game.application.features.entergame;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntergamePacketHandlerService implements PacketHandlerService<EntergamePacket> {

  private final Mediator mediator;

  @Override
  public void handle(EntergamePacket packet, Session session) {
    mediator.sendAsync(new EntergameCommand(session.getId()));
  }

  @Override
  public Class<EntergamePacket> getPacketType() {
    return EntergamePacket.class;
  }
}
