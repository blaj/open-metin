package com.blaj.openmetin.game.application.features.createcharacter;

import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateCharacterPacketHandlerService
    implements PacketHandlerService<CreateCharacterPacket> {

  private final Mediator mediator;

  @Override
  public void handle(CreateCharacterPacket packet, Session session) {
    mediator.sendAsync(
        new CreateCharacterCommand(
            packet.getSlot(),
            packet.getName(),
            ClassType.fromValue(packet.getClassType()),
            packet.getShape(),
            session.getId()));
  }

  @Override
  public Class<CreateCharacterPacket> getPacketType() {
    return CreateCharacterPacket.class;
  }
}
