package com.blaj.openmetin.game.application.features.deletecharacter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.application.common.account.AccountDto;
import com.blaj.openmetin.game.application.common.account.AccountRestClientService;
import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.game.shared.constants.CharacterConstants;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import io.netty.channel.Channel;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteCharacterCommandHandlerTest {

  private DeleteCharacterCommandHandler deleteCharacterCommandHandler;

  @Mock private SessionManagerService<GameSession> sessionManagerService;
  @Mock private SessionService sessionService;
  @Mock private CharacterService characterService;
  @Mock private AccountRestClientService accountRestClientService;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    deleteCharacterCommandHandler =
        new DeleteCharacterCommandHandler(
            sessionManagerService, sessionService, characterService, accountRestClientService);
  }

  @Test
  public void givenNonExistingSession_whenHandle_thenThrowException() {
    // given
    var sessionId = 123L;
    var deleteCharacterCommand = new DeleteCharacterCommand((short) 1, "1234567 ", sessionId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.empty());

    // when
    var thrownException =
        assertThrows(
            EntityNotFoundException.class,
            () -> deleteCharacterCommandHandler.handle(deleteCharacterCommand));

    // then
    assertThat(thrownException).hasMessageContaining("Session not exists");
  }

  @Test
  public void givenNonExistingAccountId_whenHandle_thenSessionChannelClose() {
    // given
    var sessionId = 123L;
    var deleteCharacterCommand = new DeleteCharacterCommand((short) 1, "1234567 ", sessionId);
    var gameSession = new GameSession(sessionId, channel);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    deleteCharacterCommandHandler.handle(deleteCharacterCommand);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenNonExistingCharacter_whenHandle_thenThrowException() {
    // given
    var sessionId = 123L;
    var accountId = 321L;
    var deleteCharacterCommand = new DeleteCharacterCommand((short) 1, "1234567 ", sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(characterService.getCharacter(accountId, deleteCharacterCommand.slot()))
        .willReturn(Optional.empty());

    // when
    var thrownException =
        assertThrows(
            EntityNotFoundException.class,
            () -> deleteCharacterCommandHandler.handle(deleteCharacterCommand));

    // then
    assertThat(thrownException).hasMessageContaining("Character not exists");
  }

  @Test
  public void givenCharacterLevelGreaterThanDeletableLevel_whenHandle_thenSendFailurePacket() {
    // given
    var sessionId = 123L;
    var accountId = 321L;
    var deleteCharacterCommand = new DeleteCharacterCommand((short) 1, "1234567 ", sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);
    var characterDto =
        CharacterDto.builder()
            .id(123L)
            .name("name")
            .classType(ClassType.NINJA_FEMALE)
            .level(CharacterConstants.CHARACTER_DELETE_LEVEL_LIMIT + 1)
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
    given(characterService.getCharacter(accountId, deleteCharacterCommand.slot()))
        .willReturn(Optional.of(characterDto));

    // when
    deleteCharacterCommandHandler.handle(deleteCharacterCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new DeleteCharacterFailurePacket().setType((short) 1));
  }

  @Test
  public void givenNonExistingAccount_whenHandle_thenSessionChannelClose() {
    // given
    var sessionId = 123L;
    var accountId = 321L;
    var deleteCharacterCommand = new DeleteCharacterCommand((short) 1, "1234567 ", sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);
    var characterDto =
        CharacterDto.builder()
            .id(123L)
            .name("name")
            .classType(ClassType.NINJA_FEMALE)
            .level(CharacterConstants.CHARACTER_DELETE_LEVEL_LIMIT)
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
    given(characterService.getCharacter(accountId, deleteCharacterCommand.slot()))
        .willReturn(Optional.of(characterDto));
    given(accountRestClientService.getAccountCached(accountId)).willReturn(null);

    // when
    deleteCharacterCommandHandler.handle(deleteCharacterCommand);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenNonMatchingDeleteCode_whenHandle_thenSendFailurePacket() {
    // given
    var sessionId = 123L;
    var accountId = 321L;
    var deleteCharacterCommand = new DeleteCharacterCommand((short) 1, "1234567 ", sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);
    var characterDto =
        CharacterDto.builder()
            .id(123L)
            .name("name")
            .classType(ClassType.NINJA_FEMALE)
            .level(CharacterConstants.CHARACTER_DELETE_LEVEL_LIMIT)
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
    var accountDto = new AccountDto(accountId, "username", "mail@mail.com", "7654321");

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(characterService.getCharacter(accountId, deleteCharacterCommand.slot()))
        .willReturn(Optional.of(characterDto));
    given(accountRestClientService.getAccountCached(accountId)).willReturn(accountDto);

    // when
    deleteCharacterCommandHandler.handle(deleteCharacterCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new DeleteCharacterFailurePacket().setType((short) 1));
  }

  @Test
  public void givenValid_whenHandle_thenCharacterDeleteAndSendSuccessPacket() {
    // given
    var sessionId = 123L;
    var accountId = 321L;
    var deleteCharacterCommand = new DeleteCharacterCommand((short) 1, "1234567 ", sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);
    var characterDto =
        CharacterDto.builder()
            .id(123L)
            .name("name")
            .classType(ClassType.NINJA_FEMALE)
            .level(CharacterConstants.CHARACTER_DELETE_LEVEL_LIMIT)
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
    var accountDto = new AccountDto(accountId, "username", "mail@mail.com", "1234567");

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(characterService.getCharacter(accountId, deleteCharacterCommand.slot()))
        .willReturn(Optional.of(characterDto));
    given(accountRestClientService.getAccountCached(accountId)).willReturn(accountDto);

    // when
    deleteCharacterCommandHandler.handle(deleteCharacterCommand);

    // then
    then(characterService).should().delete(accountId, deleteCharacterCommand.slot());

    then(sessionService)
        .should()
        .sendPacketAsync(
            sessionId, new DeleteCharacterSuccessPacket().setSlot(deleteCharacterCommand.slot()));
  }
}
