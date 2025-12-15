package com.blaj.openmetin.game.application.features.handshake;

import com.blaj.openmetin.game.application.common.eventsystem.EventSystemService;
import com.blaj.openmetin.game.application.common.ping.PingPacket;
import com.blaj.openmetin.shared.application.features.handshake.BaseHandshakeCommandHandler;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.model.Session;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class GameHandshakeCommandHandlerService extends BaseHandshakeCommandHandler {

  private static final Duration pingDuration = Duration.ofSeconds(5);

  private final SessionService sessionService;
  private final EventSystemService eventSystemService;

  public GameHandshakeCommandHandlerService(
      SessionService sessionService,
      SessionManagerService sessionManagerService,
      EventSystemService eventSystemService) {
    super(sessionService, sessionManagerService);
    this.sessionService = sessionService;
    this.eventSystemService = eventSystemService;
  }

  @Override
  protected void onSuccessHandshake(Session session) {
    session.setPhase(Phase.LOGIN);
    sessionService.sendPacketAsync(session.getId(), new PhasePacket().setPhase(session.getPhase()));

    eventSystemService.scheduleEvent(
        () -> {
          sessionService.sendPacketAsync(session.getId(), new PingPacket());
          return pingDuration;
        },
        pingDuration);
  }
}
