package com.blaj.openmetin.game.application.features.clientversion;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientVersionPacketHandlerService
    implements PacketHandlerService<ClientVersionPacket> {

  @Override
  public void handle(ClientVersionPacket packet, Session session) {
    log.info("Received client version: {}, {}", packet.getExecutableName(), packet.getTimestamp());
  }

  @Override
  public Class<ClientVersionPacket> getPacketType() {
    return ClientVersionPacket.class;
  }
}
