package com.blaj.openmetin.authentication.application.features.login;

import com.blaj.openmetin.authentication.application.common.contract.AuthenticationCloseConnectionEvent;
import com.blaj.openmetin.authentication.application.common.port.AuthenticationAmqpEvents;
import com.blaj.openmetin.authentication.domain.entity.Account;
import com.blaj.openmetin.authentication.domain.repository.AccountRepository;
import com.blaj.openmetin.shared.common.abstractions.SessionService;
import com.blaj.openmetin.shared.domain.entity.LoginToken;
import com.blaj.openmetin.shared.domain.repository.LoginTokenRepository;
import com.blaj.openmetin.shared.infrastructure.cqrs.RequestHandler;
import com.blaj.openmetin.shared.infrastructure.encryption.HandshakeUtils;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginCommandHandlerService implements RequestHandler<LoginCommand, Void> {

  private static final String statusWrongPassword = "WRONGPWD";
  private static final String statusNotFound = "NOID";
  private static final String statusNotAvailable = "NOTAVAIL";
  private static final String statusAlreadyLogged = "ALREADY";
  private static final String statusServerFull = "FULL";

  private static final int closeConnectionAttempts = 3;

  private final AccountRepository accountRepository;
  private final SessionService sessionService;
  private final PasswordEncoder passwordEncoder;
  private final LoginTokenRepository loginTokenRepository;
  private final AuthenticationAmqpEvents authenticationAmqpEvents;

  @Override
  public Void handle(LoginCommand request) {
    var account = accountRepository.findByUsername(request.username()).orElse(null);

    if (account == null) {
      log.debug("Account {} not found", request.username());
      sessionService.sendPacketAsync(
          request.sessionId(), new LoginFailedPacket().setStatus(statusNotFound));
      return null;
    }

    if (!passwordEncoder.matches(request.password(), account.getPassword())) {
      log.debug("Wrong password supplied for account {}", request.username());
      sessionService.sendPacketAsync(
          request.sessionId(), new LoginFailedPacket().setStatus(statusWrongPassword));
      return null;
    }

    var status =
        Optional.of(loginTokenRepository.loginKeyExists(account.getId()))
            .filter(isAccountLogged -> isAccountLogged)
            .map(_ -> decideAccountLoggedStatus(account))
            .orElse("");

    if (!status.isEmpty()) {
      sessionService.sendPacketAsync(
          request.sessionId(), new LoginFailedPacket().setStatus(status));
      return null;
    }

    var loginKey = HandshakeUtils.generateUInt32();

    loginTokenRepository.saveLoginToken(
        loginKey,
        LoginToken.builder()
            .accountId(account.getId())
            .username(request.username())
            .socketAddress(request.socketAddress())
            .build());

    loginTokenRepository.saveLoginKey(account.getId(), loginKey);

    sessionService.sendPacketAsync(
        request.sessionId(), new LoginSuccessPacket().setKey(loginKey).setResult((byte) 1));

    return null;
  }

  private String decideAccountLoggedStatus(Account account) {
    var attempts = loginTokenRepository.getAttempts(account.getId());

    if (attempts != null && attempts <= closeConnectionAttempts) {
      return statusAlreadyLogged;
    }

    authenticationAmqpEvents.publish(new AuthenticationCloseConnectionEvent(account.getId()));

    loginTokenRepository.deleteLoginKey(account.getId());
    loginTokenRepository.deleteAttempts(account.getId());

    return null;
  }
}
