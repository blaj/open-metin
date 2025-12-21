package com.blaj.openmetin.game.application.features.selectcharacter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.application.common.character.dto.CharacterBasicDataPacket;
import com.blaj.openmetin.game.application.common.character.dto.CharacterPointsPacket;
import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.application.common.character.service.GameCharacterEntityCalculationService;
import com.blaj.openmetin.game.application.common.character.service.GameCharacterEntityFactoryService;
import com.blaj.openmetin.game.application.common.character.service.GameCharacterEntityLoaderService;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.model.CharacterDto;
import com.blaj.openmetin.game.domain.model.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.GameSession;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import io.netty.channel.Channel;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SelectCharacterCommandHandlerServiceTest {

  private SelectCharacterCommandHandlerService selectCharacterCommandHandlerService;

  @Mock private SessionManagerService<GameSession> sessionManagerService;
  @Mock private SessionService sessionService;
  @Mock private CharacterService characterService;
  @Mock private GameCharacterEntityFactoryService gameCharacterEntityFactoryService;
  @Mock private GameCharacterEntityLoaderService gameCharacterEntityLoaderService;
  @Mock private GameCharacterEntityCalculationService gameCharacterEntityCalculationService;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    selectCharacterCommandHandlerService =
        new SelectCharacterCommandHandlerService(
            sessionManagerService,
            sessionService,
            characterService,
            gameCharacterEntityFactoryService,
            gameCharacterEntityLoaderService,
            gameCharacterEntityCalculationService);
  }

  @Test
  public void givenNonExistingSession_whenHandle_thenThrowException() {
    // given
    var sessionId = 123L;
    var selectCharacterCommand = new SelectCharacterCommand((short) 1, sessionId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.empty());

    // when
    var thrownException =
        assertThrows(
            EntityNotFoundException.class,
            () -> selectCharacterCommandHandlerService.handle(selectCharacterCommand));

    // then
    assertThat(thrownException).hasMessageContaining("Session not exists");
  }

  @Test
  public void givenNonExistingAccountId_whenHandle_thenSessionChannelClose() {
    // given
    var sessionId = 123L;
    var selectCharacterCommand = new SelectCharacterCommand((short) 1, sessionId);
    var gameSession = new GameSession(sessionId, channel);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));

    // when
    selectCharacterCommandHandlerService.handle(selectCharacterCommand);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenNonExistingCharacter_whenHandle_thenThrowException() {
    // given
    var sessionId = 123L;
    var accountId = 33L;
    var selectCharacterCommand = new SelectCharacterCommand((short) 1, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(characterService.getCharacter(accountId, selectCharacterCommand.slot()))
        .willReturn(Optional.empty());

    // when
    var thrownException =
        assertThrows(
            EntityNotFoundException.class,
            () -> selectCharacterCommandHandlerService.handle(selectCharacterCommand));

    // then
    assertThat(thrownException).hasMessageContaining("Character not exists");
  }

  @Test
  public void givenValid_whenHandle_thenSuccessPacketsSend() {
    // given
    var sessionId = 123L;
    var accountId = 33L;
    var selectCharacterCommand = new SelectCharacterCommand((short) 1, sessionId);
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
    var gameCharacterEntity =
        GameCharacterEntity.builder()
            .session(gameSession)
            .characterDto(characterDto)
            .empire(characterDto.getEmpire())
            .positionX(characterDto.getPositionX())
            .positionY(characterDto.getPositionY())
            .build();

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(characterService.getCharacter(accountId, selectCharacterCommand.slot()))
        .willReturn(Optional.of(characterDto));
    given(gameCharacterEntityFactoryService.create(gameSession, characterDto))
        .willReturn(gameCharacterEntity);

    // when
    selectCharacterCommandHandlerService.handle(selectCharacterCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new PhasePacket().setPhase(Phase.LOADING));
    then(gameCharacterEntityLoaderService).should().load(gameCharacterEntity);
    then(gameCharacterEntityCalculationService).should().calculate(gameCharacterEntity);
    then(sessionService)
        .should()
        .sendPacketAsync(
            sessionId,
            new CharacterBasicDataPacket()
                .setVid(gameCharacterEntity.getVid())
                .setName(gameCharacterEntity.getCharacterDto().getName())
                .setClassType(gameCharacterEntity.getCharacterDto().getClassType().getValue())
                .setPositionX(gameCharacterEntity.getPositionX())
                .setPositionY(gameCharacterEntity.getPositionY())
                .setEmpire(gameCharacterEntity.getEmpire())
                .setSkillGroup(gameCharacterEntity.getCharacterDto().getSkillGroup().shortValue()));
    then(sessionService).should().sendPacketAsync(sessionId, new CharacterPointsPacket());
  }
}
