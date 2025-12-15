package com.blaj.openmetin.authentication.infrastructure.amqp;

import com.blaj.openmetin.authentication.application.common.contract.AuthenticationCloseConnectionEvent;
import com.blaj.openmetin.authentication.application.features.closeconnection.CloseConnectionCommand;
import com.blaj.openmetin.authentication.infrastructure.config.AmqpConfig;
import com.blaj.openmetin.shared.infrastructure.cqrs.Mediator;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationListenerAmqp {

  private final Mediator mediator;

  @RabbitListener(queues = AmqpConfig.QUEUE_AUTHENTICATION_CLOSE_CONNECTION)
  public void handle(AuthenticationCloseConnectionEvent authenticationCloseConnectionEvent) {
    mediator.send(new CloseConnectionCommand(authenticationCloseConnectionEvent.accountId()));
  }
}
