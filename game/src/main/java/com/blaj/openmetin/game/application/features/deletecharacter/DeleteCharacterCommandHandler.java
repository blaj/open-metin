package com.blaj.openmetin.game.application.features.deletecharacter;

import com.blaj.openmetin.game.application.common.account.AccountRestClientService;
import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.game.shared.constants.CharacterConstants;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCharacterCommandHandler implements RequestHandler<DeleteCharacterCommand, Void> {

  private final SessionManagerService<GameSession> sessionManagerService;
  private final SessionService sessionService;
  private final CharacterService characterService;
  private final AccountRestClientService accountRestClientService;

  @Override
  public Void handle(DeleteCharacterCommand request) {
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

    if (character.getLevel() > CharacterConstants.CHARACTER_DELETE_LEVEL_LIMIT) {
      sessionService.sendPacketAsync(
          session.getId(), new DeleteCharacterFailurePacket().setType((short) 1));
      return null;
    }

    var account = accountRestClientService.getAccountCached(session.getAccountId());

    if (account == null) {
      session.getChannel().close();
      return null;
    }

    var deleteCodeFromRequest =
        request.deleteCode().substring(0, request.deleteCode().length() - 1);

    if (!account.deleteCode().equals(deleteCodeFromRequest)) {
      sessionService.sendPacketAsync(
          session.getId(), new DeleteCharacterFailurePacket().setType((short) 1));
      return null;
    }

    characterService.delete(account.id(), request.slot());

    sessionService.sendPacketAsync(
        session.getId(), new DeleteCharacterSuccessPacket().setSlot(request.slot()));

    return null;
  }
}
