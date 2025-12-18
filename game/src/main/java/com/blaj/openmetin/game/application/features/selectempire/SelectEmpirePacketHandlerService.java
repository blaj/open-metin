package com.blaj.openmetin.game.application.features.selectempire;

import com.blaj.openmetin.game.application.common.empire.EmpirePacket;
import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SelectEmpirePacketHandlerService implements PacketHandlerService<EmpirePacket> {

  @Override
  public void handle(EmpirePacket packet, Session session) {}

  @Override
  public Class<EmpirePacket> getPacketType() {
    return EmpirePacket.class;
  }
}
