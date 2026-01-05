package com.blaj.openmetin.game.application.features.tokenlogin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.game.application.common.character.dto.CharacterListPacket;
import com.blaj.openmetin.game.application.common.character.service.CharacterService;
import com.blaj.openmetin.game.application.common.empire.EmpirePacket;
import com.blaj.openmetin.game.domain.enums.character.ClassType;
import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.application.common.config.TcpConfig;
import com.blaj.openmetin.shared.application.features.phase.PhasePacket;
import com.blaj.openmetin.shared.common.abstractions.SessionManagerService;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.common.enums.Phase;
import com.blaj.openmetin.shared.domain.entity.LoginToken;
import com.blaj.openmetin.shared.domain.repository.LoginTokenRepository;
import io.netty.channel.Channel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenLoginCommandHandlerServiceTest {

  private TokenLoginCommandHandlerService tokenLoginCommandHandlerService;

  @Mock private LoginTokenRepository loginTokenRepository;
  @Mock private SessionManagerService<GameSession> sessionManagerService;
  @Mock private SessionService sessionService;
  @Mock private CharacterService characterService;
  @Mock private TcpConfig tcpConfig;
  @Mock private Channel channel;

  @BeforeEach
  public void beforeEach() {
    tokenLoginCommandHandlerService =
        new TokenLoginCommandHandlerService(
            loginTokenRepository,
            sessionManagerService,
            sessionService,
            characterService,
            tcpConfig);
  }

  @Test
  public void givenNonExistingSession_whenHandle_thenDoNothing() {
    // given
    var tokenLoginCommand =
        new TokenLoginCommand("username", 32523L, new long[] {12, 436, 765, 321}, 32L);

    given(sessionManagerService.getSession(tokenLoginCommand.sessionId()))
        .willReturn(Optional.empty());

    // when
    tokenLoginCommandHandlerService.handle(tokenLoginCommand);

    // then
    then(sessionService).should(never()).sendPacketSync(anyLong(), any());
    then(sessionService).should(never()).sendPacketAsync(anyLong(), any());
    then(characterService).should(never()).getCharacters(anyLong());
  }

  @Test
  public void givenNonExistingLoginToken_whenHandle_thenChannelClose() {
    // given
    var tokenLoginCommand =
        new TokenLoginCommand("username", 32523L, new long[] {12, 436, 765, 321}, 32L);
    var gameSession = new GameSession(tokenLoginCommand.sessionId(), channel);

    given(sessionManagerService.getSession(tokenLoginCommand.sessionId()))
        .willReturn(Optional.of(gameSession));
    given(loginTokenRepository.getLoginToken(tokenLoginCommand.key())).willReturn(null);

    // when
    tokenLoginCommandHandlerService.handle(tokenLoginCommand);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenNonMatchingUsername_whenHandle_thenChannelClose() {
    // given
    var tokenLoginCommand =
        new TokenLoginCommand("username", 32523L, new long[] {12, 436, 765, 321}, 32L);
    var gameSession = new GameSession(tokenLoginCommand.sessionId(), channel);
    var loginToken = LoginToken.builder().username("nonMatching").build();

    given(sessionManagerService.getSession(tokenLoginCommand.sessionId()))
        .willReturn(Optional.of(gameSession));
    given(loginTokenRepository.getLoginToken(tokenLoginCommand.key())).willReturn(loginToken);

    // when
    tokenLoginCommandHandlerService.handle(tokenLoginCommand);

    // then
    then(channel).should().close();
  }

  @Test
  public void givenValid_whenHandle_thenPacketsSend() throws UnknownHostException {
    // given
    var tokenLoginCommand =
        new TokenLoginCommand("username", 32523L, new long[] {12, 436, 765, 321}, 32L);
    var gameSession = new GameSession(tokenLoginCommand.sessionId(), channel);
    var loginToken =
        LoginToken.builder().username(tokenLoginCommand.username()).accountId(123L).build();
    var characterDto1 = characterDto(1L);
    var characterDto2 = characterDto(2L);

    given(tcpConfig.host()).willReturn(InetAddress.getLocalHost());
    given(channel.localAddress())
        .willReturn(InetSocketAddress.createUnresolved("localhost", 13000));
    given(sessionManagerService.getSession(tokenLoginCommand.sessionId()))
        .willReturn(Optional.of(gameSession));
    given(loginTokenRepository.getLoginToken(tokenLoginCommand.key())).willReturn(loginToken);
    given(characterService.getCharacters(loginToken.getAccountId()))
        .willReturn(List.of(characterDto1, characterDto2));

    // when
    tokenLoginCommandHandlerService.handle(tokenLoginCommand);

    // then
    then(sessionService)
        .should()
        .sendPacketAsync(
            gameSession.getId(), new EmpirePacket().setEmpire(characterDto1.getEmpire()));
    then(sessionService)
        .should()
        .sendPacketAsync(gameSession.getId(), new PhasePacket().setPhase(Phase.SELECT_CHARACTER));
    then(sessionService)
        .should()
        .sendPacketAsync(eq(gameSession.getId()), any(CharacterListPacket.class));
  }

  private CharacterDto characterDto(long id) {
    return CharacterDto.builder()
        .id(id)
        .name("name#" + id)
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
        .slot((int) id)
        .build();
  }
}
