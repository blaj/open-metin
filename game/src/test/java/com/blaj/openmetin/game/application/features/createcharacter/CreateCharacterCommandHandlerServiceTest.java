package com.blaj.openmetin.game.application.features.createcharacter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.game.application.common.character.mapper.SimpleCharacterPacketMapper;
import com.blaj.openmetin.game.application.common.character.service.CharacterCreationTimeService;
import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.game.domain.repository.BannedWordRepository;
import com.blaj.openmetin.game.domain.repository.CharacterRepository;
import com.blaj.openmetin.game.shared.constants.CharacterConstants;
import com.blaj.openmetin.shared.application.common.config.TcpConfig;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.infrastructure.network.utils.NetworkUtils;
import io.netty.channel.Channel;
import jakarta.persistence.EntityNotFoundException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
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

  @Test
  public void givenExistingBannedWord_whenHandle_thenFailurePacketSend() {
    // given
    var sessionId = 123L;
    var accountId = 333L;
    var name = "name";
    var createCharacterCommand =
        new CreateCharacterCommand((short) 2, name, ClassType.NINJA_FEMALE, (short) 1, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(bannedWordRepository.existsByWord(name)).willReturn(true);

    // when
    createCharacterCommandHandlerService.handle(createCharacterCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new CreateCharacterFailurePacket().setError((short) 1));
  }

  @Test
  public void givenShapeGreaterThan1_whenHandle_thenFailurePacketSend() {
    // given
    var sessionId = 123L;
    var accountId = 333L;
    var name = "name";
    var createCharacterCommand =
        new CreateCharacterCommand((short) 2, name, ClassType.NINJA_FEMALE, (short) 2, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(bannedWordRepository.existsByWord(name)).willReturn(false);

    // when
    createCharacterCommandHandlerService.handle(createCharacterCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new CreateCharacterFailurePacket().setError((short) 1));
  }

  @Test
  public void givenExistingName_whenHandle_thenFailurePacketSend() {
    // given
    var sessionId = 123L;
    var accountId = 333L;
    var name = "name";
    var createCharacterCommand =
        new CreateCharacterCommand((short) 2, name, ClassType.NINJA_FEMALE, (short) 1, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(bannedWordRepository.existsByWord(name)).willReturn(false);
    given(characterRepository.existsByName(name)).willReturn(true);

    // when
    createCharacterCommandHandlerService.handle(createCharacterCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new CreateCharacterFailurePacket().setError((short) 0));
  }

  @Test
  public void givenExistingSlotAndAccountId_whenHandle_thenFailurePacketSend() {
    // given
    var sessionId = 123L;
    var accountId = 333L;
    var name = "name";
    var slot = 2;
    var createCharacterCommand =
        new CreateCharacterCommand(
            (short) slot, name, ClassType.NINJA_FEMALE, (short) 1, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(bannedWordRepository.existsByWord(name)).willReturn(false);
    given(characterRepository.existsByName(name)).willReturn(false);
    given(characterRepository.existsBySlotAndAccountId(slot, accountId)).willReturn(true);

    // when
    createCharacterCommandHandlerService.handle(createCharacterCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new CreateCharacterFailurePacket().setError((short) 0));
  }

  @Test
  public void givenMaxAccountCount_whenHandle_thenFailurePacketSend() {
    // given
    var sessionId = 123L;
    var accountId = 333L;
    var name = "name";
    var slot = 2;
    var createCharacterCommand =
        new CreateCharacterCommand(
            (short) slot, name, ClassType.NINJA_FEMALE, (short) 1, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(bannedWordRepository.existsByWord(name)).willReturn(false);
    given(characterRepository.existsByName(name)).willReturn(false);
    given(characterRepository.existsBySlotAndAccountId(slot, accountId)).willReturn(false);
    given(characterRepository.countByAccountId(accountId))
        .willReturn(CharacterConstants.MAX_CHARACTERS_PER_ACCOUNT);

    // when
    createCharacterCommandHandlerService.handle(createCharacterCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new CreateCharacterFailurePacket().setError((short) 0));
  }

  @Test
  public void givenRateLimit_whenHandle_thenFailurePacketSend() {
    // given
    var sessionId = 123L;
    var accountId = 333L;
    var name = "name";
    var slot = 2;
    var createCharacterCommand =
        new CreateCharacterCommand(
            (short) slot, name, ClassType.NINJA_FEMALE, (short) 1, sessionId);
    var gameSession = new GameSession(sessionId, channel);
    gameSession.setAccountId(accountId);

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(bannedWordRepository.existsByWord(name)).willReturn(false);
    given(characterRepository.existsByName(name)).willReturn(false);
    given(characterRepository.existsBySlotAndAccountId(slot, accountId)).willReturn(false);
    given(characterRepository.countByAccountId(accountId)).willReturn(1);
    given(
            characterCreationTimeService.tryConsume(
                accountId,
                Duration.ofSeconds(
                    CharacterConstants.INTERVAL_BETWEEN_CHARACTER_CREATE_IN_SECONDS)))
        .willReturn(false);

    // when
    createCharacterCommandHandlerService.handle(createCharacterCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(sessionId, new CreateCharacterFailurePacket().setError((short) 1));
  }

  @Test
  public void givenValid_whenHandle_thenCreateCharacterAndSendSuccessPacket()
      throws UnknownHostException {
    // given
    var sessionId = 123L;
    var accountId = 333L;
    var name = "name";
    var slot = 2;
    var createCharacterCommand =
        new CreateCharacterCommand(
            (short) slot, name, ClassType.NINJA_FEMALE, (short) 1, sessionId);
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
            .build();

    given(sessionManagerService.getSession(sessionId)).willReturn(Optional.of(gameSession));
    given(bannedWordRepository.existsByWord(name)).willReturn(false);
    given(characterRepository.existsByName(name)).willReturn(false);
    given(characterRepository.existsBySlotAndAccountId(slot, accountId)).willReturn(false);
    given(characterRepository.countByAccountId(accountId)).willReturn(1);
    given(
            characterCreationTimeService.tryConsume(
                accountId,
                Duration.ofSeconds(
                    CharacterConstants.INTERVAL_BETWEEN_CHARACTER_CREATE_IN_SECONDS)))
        .willReturn(true);
    given(
            characterService.create(
                accountId,
                name,
                createCharacterCommand.classType(),
                createCharacterCommand.shape(),
                (short) slot))
        .willReturn(characterDto);
    given(channel.localAddress())
        .willReturn(InetSocketAddress.createUnresolved("127.0.0.1", 12451));
    given(tcpConfig.host()).willReturn(Inet4Address.getLocalHost());
    given(tcpConfig.port()).willReturn(13000);

    // when
    createCharacterCommandHandlerService.handle(createCharacterCommand);

    // then
    var simpleCharacterPacket = SimpleCharacterPacketMapper.map(characterDto);
    simpleCharacterPacket.setIp(
        NetworkUtils.ipToInt(
            NetworkUtils.resolveAdvertisedAddress(
                tcpConfig.host(), NetworkUtils.getLocalAddress(gameSession.getChannel()))));
    simpleCharacterPacket.setPort(tcpConfig.port());

    then(sessionService)
        .should()
        .sendPacketAsync(
            sessionId,
            new CreateCharacterSuccessPacket()
                .setSlot((short) slot)
                .setSimpleCharacterPacket(simpleCharacterPacket));
  }
}
