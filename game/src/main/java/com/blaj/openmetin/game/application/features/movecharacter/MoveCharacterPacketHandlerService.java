package com.blaj.openmetin.game.application.features.movecharacter;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MoveCharacterPacketHandlerService
    implements PacketHandlerService<MoveCharacterPacket> {

  private final Mediator mediator;

  @Override
  public void handle(MoveCharacterPacket packet, Session session) {
    mediator.sendAsync(
        new MoveCharacterCommand(
            packet.getMovementType(),
            packet.getArgument(),
            packet.getRotation(),
            packet.getPositionX(),
            packet.getPositionY(),
            packet.getTime(),
            session.getId()));
  }

  @Override
  public Class<MoveCharacterPacket> getPacketType() {
    return MoveCharacterPacket.class;
  }
}
