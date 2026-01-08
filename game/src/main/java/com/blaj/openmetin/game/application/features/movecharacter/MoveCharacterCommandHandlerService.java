package com.blaj.openmetin.game.application.features.movecharacter;

import com.blaj.openmetin.game.application.common.character.dto.MoveCharacterBroadcastPacket;
import com.blaj.openmetin.game.application.common.entity.GameEntityMovementService;
import com.blaj.openmetin.game.domain.enums.character.CharacterMovementType;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.common.utils.DateTimeUtils;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoveCharacterCommandHandlerService
    implements RequestHandler<MoveCharacterCommand, Void> {

  private final SessionManagerService<GameSession> sessionManagerService;
  private final SessionService sessionService;
  private final GameEntityMovementService gameEntityMovementService;

  @Override
  public Void handle(MoveCharacterCommand request) {
    var session =
        sessionManagerService
            .getSession(request.sessionId())
            .orElseThrow(() -> new EntityNotFoundException("Session not exists"));

    if (session.getGameCharacterEntity() == null) {
      log.error("Game character not exists");
      session.getChannel().close();
      return null;
    }

    if (request.movementType() == null) {
      log.error("Received unknown movement type");
      session.getChannel().close();
      return null;
    }

    if (request.movementType() == CharacterMovementType.MOVE) {
      session.getGameCharacterEntity().setRotation(request.rotation() * 5);

      gameEntityMovementService.goTo(
          session.getGameCharacterEntity(),
          request.positionX(),
          request.positionY(),
          DateTimeUtils.getUnixTime());
    }

    if (request.movementType() == CharacterMovementType.WAIT) {
      gameEntityMovementService.wait(
          session.getGameCharacterEntity(), request.positionX(), request.positionY());
    }

    var moveCharacterBroadcastPacket =
        new MoveCharacterBroadcastPacket()
            .setMovementType(request.movementType())
            .setArgument(request.argument())
            .setRotation(request.rotation())
            .setVid(session.getGameCharacterEntity().getVid())
            .setPositionX(request.positionX())
            .setPositionY(request.positionY())
            .setTime(request.time())
            .setDuration(
                request.movementType() == CharacterMovementType.MOVE
                    ? session.getGameCharacterEntity().getMovementDuration()
                    : 0);

    session.getGameCharacterEntity().getNearbyEntities().stream()
        .filter(nearbyEntity -> nearbyEntity instanceof GameCharacterEntity)
        .map(nearbyEntity -> (GameCharacterEntity) nearbyEntity)
        .map(GameCharacterEntity::getSession)
        .map(Session::getId)
        .forEach(
            sessionId -> sessionService.sendPacketAsync(sessionId, moveCharacterBroadcastPacket));

    return null;
  }
}
