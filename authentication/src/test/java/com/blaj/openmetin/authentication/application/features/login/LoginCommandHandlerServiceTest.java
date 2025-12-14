package com.blaj.openmetin.authentication.application.features.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.authentication.application.common.contract.AuthenticationCloseConnectionEvent;
import com.blaj.openmetin.authentication.application.common.port.AuthenticationAmqpEvents;
import com.blaj.openmetin.authentication.domain.entity.Account;
import com.blaj.openmetin.authentication.domain.repository.AccountRepository;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.domain.repository.LoginTokenRepository;
import com.blaj.openmetin.shared.infrastructure.encryption.HandshakeUtils;
import java.net.InetSocketAddress;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class LoginCommandHandlerServiceTest {

  private LoginCommandHandlerService loginCommandHandlerService;

  @Mock private AccountRepository accountRepository;
  @Mock private SessionService sessionService;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private LoginTokenRepository loginTokenRepository;
  @Mock private AuthenticationAmqpEvents authenticationAmqpEvents;

  @Captor private ArgumentCaptor<LoginFailedPacket> loginFailedPacketCaptor;
  @Captor private ArgumentCaptor<LoginSuccessPacket> loginSuccessPacketCaptor;

  @BeforeEach
  public void beforeEach() {
    loginCommandHandlerService =
        new LoginCommandHandlerService(
            accountRepository,
            sessionService,
            passwordEncoder,
            loginTokenRepository,
            authenticationAmqpEvents);
  }

  @Test
  public void givenAccountNotFound_whenHandle_thenSendsNotFoundStatus() {
    // given
    var sessionId = 123L;
    var remoteAddress = InetSocketAddress.createUnresolved("localhost", 123);
    var loginCommand =
        new LoginCommand("user", "pass", new long[] {1, 2, 3, 4}, sessionId, remoteAddress);

    given(accountRepository.findByUsername("user")).willReturn(Optional.empty());

    // when
    loginCommandHandlerService.handle(loginCommand);

    // then
    then(sessionService).should().sendPacketAsync(eq(sessionId), loginFailedPacketCaptor.capture());

    var packet = loginFailedPacketCaptor.getValue();
    assertThat(packet.getStatus()).isEqualTo("NOID");
  }

  @Test
  public void givenWrongPassword_whenHandle_thenSendsWrongPasswordStatus() {
    // given
    var sessionId = 123L;
    var remoteAddress = InetSocketAddress.createUnresolved("localhost", 123);
    var loginCommand =
        new LoginCommand(
            "user", "wrongPassword", new long[] {1, 2, 3, 4}, sessionId, remoteAddress);
    var account = Account.builder().password("password").build();

    given(accountRepository.findByUsername(loginCommand.username()))
        .willReturn(Optional.of(account));
    given(passwordEncoder.matches(loginCommand.password(), account.getPassword()))
        .willReturn(false);

    // when
    loginCommandHandlerService.handle(loginCommand);

    // then
    then(sessionService).should().sendPacketAsync(eq(sessionId), loginFailedPacketCaptor.capture());

    var packet = loginFailedPacketCaptor.getValue();
    assertThat(packet.getStatus()).isEqualTo("WRONGPWD");
  }

  @Test
  public void givenAccountAlreadyLoggedWithFewAttempts_whenHandle_thenSendsAlreadyLoggedStatus() {
    // given
    var sessionId = 123L;
    var remoteAddress = InetSocketAddress.createUnresolved("localhost", 123);
    var loginCommand =
        new LoginCommand(
            "user", "wrongPassword", new long[] {1, 2, 3, 4}, sessionId, remoteAddress);
    var account = Account.builder().id(123L).password("password").build();

    given(accountRepository.findByUsername(loginCommand.username()))
        .willReturn(Optional.of(account));
    given(passwordEncoder.matches(loginCommand.password(), account.getPassword())).willReturn(true);
    given(loginTokenRepository.loginKeyExists(account.getId())).willReturn(true);
    given(loginTokenRepository.getAttempts(account.getId())).willReturn(2L);

    // when
    loginCommandHandlerService.handle(loginCommand);

    // then
    then(sessionService).should().sendPacketAsync(eq(sessionId), loginFailedPacketCaptor.capture());

    var packet = loginFailedPacketCaptor.getValue();
    assertThat(packet.getStatus()).isEqualTo("ALREADY");
  }

  @Test
  public void
      givenAccountAlreadyLoggedWithManyAttempts_whenHandle_thenClosesOldConnectionAndContinues() {
    // given
    var sessionId = 123L;
    var remoteAddress = InetSocketAddress.createUnresolved("localhost", 123);
    var loginCommand =
        new LoginCommand(
            "user", "wrongPassword", new long[] {1, 2, 3, 4}, sessionId, remoteAddress);
    var account = Account.builder().id(123L).password("password").build();

    given(accountRepository.findByUsername(loginCommand.username()))
        .willReturn(Optional.of(account));
    given(passwordEncoder.matches(loginCommand.password(), account.getPassword())).willReturn(true);
    given(loginTokenRepository.loginKeyExists(account.getId())).willReturn(true);
    given(loginTokenRepository.getAttempts(account.getId())).willReturn(5L);

    try (var handshakeUtilsMock = mockStatic(HandshakeUtils.class)) {
      handshakeUtilsMock.when(HandshakeUtils::generateUInt32).thenReturn(12345L);

      // when
      loginCommandHandlerService.handle(loginCommand);

      // then
      then(authenticationAmqpEvents)
          .should()
          .publish(any(AuthenticationCloseConnectionEvent.class));
      then(loginTokenRepository).should().deleteLoginKey(account.getId());
      then(loginTokenRepository).should().deleteAttempts(account.getId());
      then(sessionService)
          .should()
          .sendPacketAsync(eq(sessionId), loginSuccessPacketCaptor.capture());
    }
  }

  @Test
  public void givenValidCredentialsAndNotLogged_whenHandle_thenSavesTokenAndSendsSuccess() {
    // given
    var sessionId = 123L;
    var remoteAddress = InetSocketAddress.createUnresolved("localhost", 123);
    var loginCommand =
        new LoginCommand(
            "user", "wrongPassword", new long[] {1, 2, 3, 4}, sessionId, remoteAddress);
    var account = Account.builder().id(123L).password("password").build();
    var handshakeUint32 = 12345L;

    given(accountRepository.findByUsername(loginCommand.username()))
        .willReturn(Optional.of(account));
    given(passwordEncoder.matches(loginCommand.password(), account.getPassword())).willReturn(true);
    given(loginTokenRepository.loginKeyExists(account.getId())).willReturn(false);

    try (var handshakeUtilsMock = mockStatic(HandshakeUtils.class)) {

      handshakeUtilsMock.when(HandshakeUtils::generateUInt32).thenReturn(handshakeUint32);

      // when
      loginCommandHandlerService.handle(loginCommand);

      // then
      then(loginTokenRepository).should().saveLoginToken(eq(handshakeUint32), any());
      then(loginTokenRepository).should().saveLoginKey(account.getId(), handshakeUint32);
      then(sessionService)
          .should()
          .sendPacketAsync(eq(sessionId), loginSuccessPacketCaptor.capture());

      var packet = loginSuccessPacketCaptor.getValue();
      assertThat(packet.getKey()).isEqualTo(handshakeUint32);
      assertThat(packet.getResult()).isEqualTo((byte) 1);

      then(authenticationAmqpEvents).should(never()).publish(any());
    }
  }
}
