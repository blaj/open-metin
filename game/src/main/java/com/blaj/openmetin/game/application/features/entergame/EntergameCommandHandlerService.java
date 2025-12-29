package com.blaj.openmetin.game.application.features.entergame;

import com.blaj.openmetin.game.application.common.config.ChannelPropertiesConfig;
import com.blaj.openmetin.game.application.common.entity.EntityVisibilityService;
import com.blaj.openmetin.game.application.common.game.GameWorldSpawnEntityService;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntergameCommandHandlerService implements RequestHandler<EntergameCommand, Void> {

  private final SessionManagerService<GameSession> sessionManagerService;
  private final SessionService sessionService;
  private final EntityVisibilityService entityVisibilityService;
  private final GameWorldSpawnEntityService gameWorldSpawnEntityService;
  private final ChannelPropertiesConfig channelPropertiesConfig;

  @Override
  public Void handle(EntergameCommand request) {
    var session =
        sessionManagerService
            .getSession(request.sessionId())
            .orElseThrow(() -> new EntityNotFoundException("Session not exists"));

    if (session.getAccountId() == null) {
      log.warn("Character create before authorization for session {}", session.getId());
      session.getChannel().close();
      return null;
    }

    session.setPhase(Phase.IN_GAME);

    sessionService.sendPacketAsync(session.getId(), new PhasePacket().setPhase(session.getPhase()));
    sessionService.sendPacketAsync(
        session.getId(), new GameTimePacket().setServerTime(DateTimeUtils.getUnixTime()));
    sessionService.sendPacketAsync(
        session.getId(), new ChannelPacket().setChannelNo(channelPropertiesConfig.channelIndex()));

    var gameCharacterEntity = session.getGameCharacterEntity();

    entityVisibilityService.showEntityToPlayer(gameCharacterEntity, session.getId());
    gameWorldSpawnEntityService.spawnEntity(gameCharacterEntity);

    return null;
  }
}
