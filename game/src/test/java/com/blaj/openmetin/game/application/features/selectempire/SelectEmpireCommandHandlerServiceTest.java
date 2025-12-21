package com.blaj.openmetin.game.application.features.selectempire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.application.common.empire.SelectEmpireService;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.domain.model.CharacterDto;
import com.blaj.openmetin.game.domain.model.GameSession;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import io.netty.channel.Channel;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SelectEmpireCommandHandlerServiceTest {

  private SelectEmpireCommandHandlerService selectEmpireCommandHandlerService;

  @Mock private SessionManagerService<GameSession> sessionManagerService;
  @Mock private SelectEmpireService selectEmpireService;
  @Mock private CharacterService characterService;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    selectEmpireCommandHandlerService =
        new SelectEmpireCommandHandlerService(
            sessionManagerService, selectEmpireService, characterService);
  }

  @Test
  public void givenNonExistingSession_whenHandle_thenThrowException() {
    // given
    var sessionId = 123L;
    var selectEmpireCommand = new SelectEmpireCommand(Empire.SHINSOO, sessionId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.empty());

    // when
    var thrownException =
        assertThrows(
            EntityNotFoundException.class,
            () -> selectEmpireCommandHandlerService.handle(selectEmpireCommand));

    // then
    assertThat(thrownException).hasMessageContaining("Session not exists");
  }

  @Test
  public void givenNonExistingAccountId_whenHandle_thenSessionChannelClose() {
    // given
    var sessionId = 123L;
    var selectEmpireCommand = new SelectEmpireCommand(Empire.SHINSOO, sessionId);
    var gameSession = new GameSession(sessionId, channel);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    selectEmpireCommandHandlerService.handle(selectEmpireCommand);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenNonExistingEmpire_whenHandle_thenSessionChannelClose() {
    // given
    var sessionId = 123L;
    var accountId = 321L;
    var selectEmpireCommand = new SelectEmpireCommand(null, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    selectEmpireCommandHandlerService.handle(selectEmpireCommand);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenNonExistingCharacters_whenHandle_thenSetEmpireCache() {
    // given
    var sessionId = 123L;
    var accountId = 321L;
    var selectEmpireCommand = new SelectEmpireCommand(Empire.SHINSOO, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(characterService.getCharacters(accountId)).willReturn(Collections.emptyList());

    // when
    selectEmpireCommandHandlerService.handle(selectEmpireCommand);

    // then
    then(selectEmpireService).should().setCache(accountId, selectEmpireCommand.empire());
  }

  @Test
  public void givenExistingCharacters_whenHandle_thenCharacterChangeEmpire() {
    // given
    var sessionId = 123L;
    var accountId = 321L;
    var selectEmpireCommand = new SelectEmpireCommand(Empire.SHINSOO, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);
    var characterDto =
        CharacterDto.builder()
            .id(123L)
            .name("name")
            .classType(ClassType.NINJA_FEMALE)
            .level(31)
            .playTime(32432L)
            .st(33)
            .ht(44)
            .dx(76)
            .iq(63)
            .bodyPart(47)
            .hairPart(102)
            .positionX(32423)
            .positionY(43654)
            .skillGroup(2)
            .build();

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(characterService.getCharacters(accountId)).willReturn(List.of(characterDto));

    // when
    selectEmpireCommandHandlerService.handle(selectEmpireCommand);

    // then
    then(characterService).should().changeEmpire(accountId, selectEmpireCommand.empire());
  }
}
