package com.blaj.openmetin.game.application.features.selectempire;

import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.application.common.empire.SelectEmpireService;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelectEmpireCommandHandlerService
    implements RequestHandler<SelectEmpireCommand, Void> {

  private final SessionManagerService<GameSession> sessionManagerService;
  private final SelectEmpireService selectEmpireService;
  private final CharacterService characterService;

  @Override
  public Void handle(SelectEmpireCommand request) {
    var session =
        sessionManagerService
            .getSession(request.sessionId())
            .orElseThrow(() -> new EntityNotFoundException("Session not exists"));

    if (session.getAccountId() == null) {
      log.warn("Empire select before authorization for session {}", session.getId());
      session.getChannel().close();
      return null;
    }

    if (request.empire() == null) {
      log.warn("Selected empire is null for accountId {}", session.getAccountId());
      session.getChannel().close();
      return null;
    }

    var charactersDto = characterService.getCharacters(session.getAccountId());

    if (!charactersDto.isEmpty()) {
      characterService.changeEmpire(session.getAccountId(), request.empire());
    } else {
      selectEmpireService.setCache(session.getAccountId(), request.empire());
    }

    return null;
  }
}
