package com.blaj.openmetin.game.application.features.selectcharacter;

import com.blaj.openmetin.game.application.common.character.dto.CharacterBasicDataPacket;
import com.blaj.openmetin.game.application.common.character.dto.CharacterPointsPacket;
import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.application.common.character.service.GameCharacterEntityCalculationService;
import com.blaj.openmetin.game.application.common.character.service.GameCharacterEntityFactoryService;
import com.blaj.openmetin.game.application.common.character.service.GameCharacterEntityLoaderService;
import com.blaj.openmetin.game.domain.model.GameSession;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelectCharacterCommandHandlerService
    implements RequestHandler<SelectCharacterCommand, Void> {

  private final SessionManagerService<GameSession> sessionManagerService;
  private final SessionService sessionService;
  private final CharacterService characterService;
  private final GameCharacterEntityFactoryService gameCharacterEntityFactoryService;
  private final GameCharacterEntityLoaderService gameCharacterEntityLoaderService;
  private final GameCharacterEntityCalculationService gameCharacterEntityCalculationService;

  @Override
  public Void handle(SelectCharacterCommand request) {
    var session =
        sessionManagerService
            .getSession(request.sessionId())
            .orElseThrow(() -> new EntityNotFoundException("Session not exists"));

    if (session.getAccountId() == null) {
      log.warn("Character create before authorization for session {}", session.getId());
      session.getChannel().close();
      return null;
    }

    var character =
        characterService
            .getCharacter(session.getAccountId(), request.slot())
            .orElseThrow(() -> new EntityNotFoundException("Character not exists"));

    session.setPhase(Phase.LOADING);
    sessionService.sendPacketAsync(session.getId(), new PhasePacket().setPhase(session.getPhase()));

    var gameCharacterEntity = gameCharacterEntityFactoryService.create(session, character);
    gameCharacterEntityLoaderService.load(gameCharacterEntity);
    gameCharacterEntityCalculationService.calculate(gameCharacterEntity);

    session.setGameCharacterEntity(gameCharacterEntity);

    sessionService.sendPacketAsync(
        session.getId(),
        new CharacterBasicDataPacket()
            .setVid(gameCharacterEntity.getVid())
            .setName(gameCharacterEntity.getCharacterDto().getName())
            .setClassType(gameCharacterEntity.getCharacterDto().getClassType().getValue())
            .setPositionX(gameCharacterEntity.getPositionX())
            .setPositionY(gameCharacterEntity.getPositionY())
            .setEmpire(gameCharacterEntity.getEmpire())
            .setSkillGroup(gameCharacterEntity.getCharacterDto().getSkillGroup().shortValue()));

    sessionService.sendPacketAsync(session.getId(), new CharacterPointsPacket());

    return null;
  }
}
