package com.blaj.openmetin.authentication.application.features.login;

import com.blaj.openmetin.shared.common.abstractions.PacketHandlerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import java.net.InetSocketAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginPacketHandlerService implements PacketHandlerService<LoginPacket> {

  private final Mediator mediator;

  @Override
  public void handle(LoginPacket packet, Session session) {
    mediator.sendAsync(
        new LoginCommand(
            packet.getUsername(),
            packet.getPassword(),
            packet.getEncryptKeys(),
            session.getId(),
            (InetSocketAddress) session.getChannel().remoteAddress()));
  }

  @Override
  public Class<LoginPacket> getPacketType() {
    return LoginPacket.class;
  }
}
