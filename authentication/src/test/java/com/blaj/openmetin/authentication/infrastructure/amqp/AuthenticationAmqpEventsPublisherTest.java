package com.blaj.openmetin.authentication.infrastructure.amqp;

import static org.mockito.BDDMockito.then;

import com.blaj.openmetin.authentication.application.common.contract.AuthenticationCloseConnectionEvent;
import com.blaj.openmetin.authentication.infrastructure.config.AmqpConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
public class AuthenticationAmqpEventsPublisherTest {

  private AuthenticationAmqpEventsPublisher authenticationAmqpEventsPublisher;

  @Mock private RabbitTemplate rabbitTemplate;

  @BeforeEach
  public void beforeEach() {
    authenticationAmqpEventsPublisher = new AuthenticationAmqpEventsPublisher(rabbitTemplate);
  }

  @Test
  public void givenValid_whenPublish_thenConvertAndSend() {
    // given
    var authenticationCloseConnectionEvent = new AuthenticationCloseConnectionEvent(123L);

    // when
    authenticationAmqpEventsPublisher.publish(authenticationCloseConnectionEvent);

    // then
    then(rabbitTemplate)
        .should()
        .convertAndSend(
            AmqpConfig.EXCHANGE_AUTHENTICATION,
            AmqpConfig.ROUTING_AUTHENTICATION_CLOSE_CONNECTION,
            authenticationCloseConnectionEvent);
  }
}
