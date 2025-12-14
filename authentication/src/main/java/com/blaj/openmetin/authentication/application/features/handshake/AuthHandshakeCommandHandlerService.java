package com.blaj.openmetin.authentication.application.features.handshake;

import com.blaj.openmetin.shared.application.features.handshake.BaseHandshakeCommandHandler;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.model.Session;
import org.springframework.stereotype.Service;

@Service
public class AuthHandshakeCommandHandlerService extends BaseHandshakeCommandHandler {

  private final SessionService sessionService;

  public AuthHandshakeCommandHandlerService(
      SessionService sessionService, SessionManagerService sessionManagerService) {
    super(sessionService, sessionManagerService);
    this.sessionService = sessionService;
  }

  @Override
  protected void onSuccessHandshake(Session session) {
    session.setPhase(Phase.AUTH);
    sessionService.sendPacketAsync(session.getId(), new PhasePacket().setPhase(session.getPhase()));
  }
}
