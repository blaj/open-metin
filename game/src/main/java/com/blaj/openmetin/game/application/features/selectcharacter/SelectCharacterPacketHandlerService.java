package com.blaj.openmetin.game.application.features.selectcharacter;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SelectCharacterPacketHandlerService
    implements PacketHandlerService<SelectCharacterPacket> {

  private final Mediator mediator;

  @Override
  public void handle(SelectCharacterPacket packet, Session session) {
    mediator.sendAsync(new SelectCharacterCommand(packet.getSlot(), session.getId()));
  }

  @Override
  public Class<SelectCharacterPacket> getPacketType() {
    return SelectCharacterPacket.class;
  }
}
