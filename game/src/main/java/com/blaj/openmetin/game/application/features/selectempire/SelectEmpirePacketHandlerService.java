package com.blaj.openmetin.game.application.features.selectempire;

import com.blaj.openmetin.game.application.common.empire.EmpirePacket;
import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SelectEmpirePacketHandlerService implements PacketHandlerService<EmpirePacket> {

  private final Mediator mediator;

  @Override
  public void handle(EmpirePacket packet, Session session) {
    mediator.sendAsync(new SelectEmpireCommand(packet.getEmpire(), session.getId()));
  }

  @Override
  public Class<EmpirePacket> getPacketType() {
    return EmpirePacket.class;
  }
}
