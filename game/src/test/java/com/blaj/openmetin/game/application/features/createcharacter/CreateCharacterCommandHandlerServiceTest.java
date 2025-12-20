package com.blaj.openmetin.game.application.features.createcharacter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.application.common.character.service.CharacterCreationTimeService;
import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.model.GameSession;
import com.blaj.openmetin.game.domain.repository.BannedWordRepository;
import com.blaj.openmetin.game.domain.repository.CharacterRepository;
import com.blaj.openmetin.shared.application.common.config.TcpConfig;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import io.netty.channel.Channel;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CreateCharacterCommandHandlerServiceTest {

  private CreateCharacterCommandHandlerService createCharacterCommandHandlerService;

  @Mock private SessionManagerService<GameSession> sessionManagerService;
  @Mock private SessionService sessionService;
  @Mock private CharacterService characterService;
  @Mock private CharacterCreationTimeService characterCreationTimeService;
  @Mock private CharacterRepository characterRepository;
  @Mock private BannedWordRepository bannedWordRepository;
  @Mock private TcpConfig tcpConfig;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    createCharacterCommandHandlerService =
        new CreateCharacterCommandHandlerService(
            sessionManagerService,
            sessionService,
            characterService,
            characterCreationTimeService,
            characterRepository,
            bannedWordRepository,
            tcpConfig);
  }

  @Test
  public void givenNonExistingSession_whenHandle_thenThrowException() {
    // given
    var nonExistingSessionId = 123L;
    var createCharacterCommand =
        new CreateCharacterCommand(
            (short) 2, "name", ClassType.NINJA_FEMALE, (short) 1, nonExistingSessionId);

    given(sessionManagerService.getSession(nonExistingSessionId)).willReturn(Optional.empty());

    // when
    var thrownException =
        assertThrows(
            EntityNotFoundException.class,
            () -> createCharacterCommandHandlerService.handle(createCharacterCommand));

    // then
    assertThat(thrownException).hasMessageContaining("Session not exists");
  }

  @Test
  public void givenNonExistingAccountId_whenHandle_thenSessionChannelClose() {
    // given
    var sessionId = 123L;
    var createCharacterCommand =
        new CreateCharacterCommand((short) 2, "name", ClassType.NINJA_FEMALE, (short) 1, sessionId);
    var gameSession = new GameSession(sessionId, channel);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    createCharacterCommandHandlerService.handle(createCharacterCommand);

    // then
    then(channel).should().close();
  }

  @ParameterizedTest
  @ValueSource(strings = {"n", "PHSsLCIrOIfyxQWIotlNxTBDr", "@", "#", "%"})
  public void givenInvalidName_whenHandle_thenFailurePacketSend(String name) {
    // given
    var sessionId = 123L;
    var accountId = 333L;
    var createCharacterCommand =
        new CreateCharacterCommand((short) 2, name, ClassType.NINJA_FEMALE, (short) 1, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    createCharacterCommandHandlerService.handle(createCharacterCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new CreateCharacterFailurePacket().setError((short) 1));
  }
}
