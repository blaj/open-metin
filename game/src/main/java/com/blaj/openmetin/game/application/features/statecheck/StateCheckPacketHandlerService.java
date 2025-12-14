package com.blaj.openmetin.game.application.features.statecheck;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateCheckPacketHandlerService implements PacketHandlerService<StateCheckPacket> {

  private final Mediator mediator;

  @Override
  public void handle(StateCheckPacket packet, Session session) {
    mediator.send(new StateCheckCommand(session.getId()));
  }

  @Override
  public Class<StateCheckPacket> getPacketType() {
    return StateCheckPacket.class;
  }
}
