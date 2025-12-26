package com.blaj.openmetin.game.application.features.statecheck;

import com.blaj.openmetin.game.domain.repository.ServerStatusRepository;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import lombok.RequiredArgsConstructor;
import org.joou.UInteger;
import org.joou.UShort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateCheckCommandHandlerService implements RequestHandler<StateCheckCommand, Void> {

  private final SessionService sessionService;
  private final ServerStatusRepository serverStatusRepository;

  @Override
  public Void handle(StateCheckCommand request) {
    var serverStatusesToSend =
        serverStatusRepository.getServerStatuses().stream()
            .map(
                serverStatus ->
                    new ServerStatusPacket.ServerStatus()
                        .setStatus(serverStatus.getStatus())
                        .setPort(serverStatus.getPort()))
            .toArray(ServerStatusPacket.ServerStatus[]::new);

    sessionService.sendPacketAsync(
        request.sessionId(),
        new ServerStatusPacket()
            .setStatuses(serverStatusesToSend)
            .setSize(UInteger.valueOf(serverStatusesToSend.length)));

    return null;
  }
}
