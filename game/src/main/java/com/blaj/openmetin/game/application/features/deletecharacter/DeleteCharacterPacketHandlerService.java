package com.blaj.openmetin.game.application.features.deletecharacter;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteCharacterPacketHandlerService
    implements PacketHandlerService<DeleteCharacterPacket> {

  private final Mediator mediator;

  @Override
  public void handle(DeleteCharacterPacket packet, Session session) {
    mediator.sendAsync(
        new DeleteCharacterCommand(packet.getSlot(), packet.getDeleteCode(), session.getId()));
  }

  @Override
  public Class<DeleteCharacterPacket> getPacketType() {
    return DeleteCharacterPacket.class;
  }
}
