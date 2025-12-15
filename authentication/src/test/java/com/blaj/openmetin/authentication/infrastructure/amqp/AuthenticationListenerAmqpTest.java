package com.blaj.openmetin.authentication.infrastructure.amqp;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.authentication.application.common.contract.AuthenticationCloseConnectionEvent;
import com.blaj.openmetin.authentication.application.features.closeconnection.CloseConnectionCommand;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthenticationListenerAmqpTest {

  private AuthenticationListenerAmqp authenticationListenerAmqp;

  @Mock private Mediator mediator;

  @BeforeEach
  public void beforeEach() {
    authenticationListenerAmqp = new AuthenticationListenerAmqp(mediator);
  }

  @Test
  public void givenValid_whenHandle_thenMediatorSend() {
    // given
    var authenticationCloseConnectionEvent = new AuthenticationCloseConnectionEvent(123L);

    // when
    authenticationListenerAmqp.handle(authenticationCloseConnectionEvent);

    // then
    then(mediator)
        .should()
        .send(eq(new CloseConnectionCommand(authenticationCloseConnectionEvent.accountId())));
  }
}
