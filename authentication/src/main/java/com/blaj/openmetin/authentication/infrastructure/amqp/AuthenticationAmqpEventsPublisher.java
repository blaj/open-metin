package com.blaj.openmetin.authentication.infrastructure.amqp;

import com.blaj.openmetin.authentication.application.common.contract.AuthenticationCloseConnectionEvent;
import com.blaj.openmetin.authentication.application.common.port.AuthenticationAmqpEvents;
import com.blaj.openmetin.authentication.infrastructure.config.AmqpConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationAmqpEventsPublisher implements AuthenticationAmqpEvents {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(AuthenticationCloseConnectionEvent authenticationCloseConnectionEvent) {
    rabbitTemplate.convertAndSend(
        AmqpConfig.EXCHANGE_AUTHENTICATION,
        AmqpConfig.ROUTING_AUTHENTICATION_CLOSE_CONNECTION,
        authenticationCloseConnectionEvent);
  }
}
