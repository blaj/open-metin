package com.blaj.openmetin.game.application.features.serverstatus;

import com.blaj.openmetin.game.domain.entity.ServerStatus;
import com.blaj.openmetin.game.domain.entity.ServerStatus.Status;
import com.blaj.openmetin.game.domain.repository.ServerStatusRepository;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import com.blaj.openmetin.shared.infrastructure.network.properties.TcpProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServerStatusCommandHandlerService
    implements RequestHandler<ServerStatusCommand, Void> {

  private final ServerStatusRepository serverStatusRepository;
  private final TcpProperties tcpProperties;

  @Override
  public Void handle(ServerStatusCommand request) {
    serverStatusRepository.saveServerStatus(
        ServerStatus.builder()
            .channelIndex(request.channelIndex())
            .port(tcpProperties.port())
            .status(Status.NORMAL)
            .build());

    return null;
  }
}
