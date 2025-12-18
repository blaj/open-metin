package com.blaj.openmetin.authentication.application.features.closeconnection;

import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloseConnectionCommandHandlerService
    implements RequestHandler<CloseConnectionCommand, Void> {

  private final SessionManagerService<Session> sessionManagerService;

  @Override
  public Void handle(CloseConnectionCommand request) {
    sessionManagerService
        .getSessionByAccountId(request.accountId())
        .ifPresent(
            session -> {
              session.getChannel().close();
              sessionManagerService.removeSession(session.getId());
            });

    return null;
  }
}
