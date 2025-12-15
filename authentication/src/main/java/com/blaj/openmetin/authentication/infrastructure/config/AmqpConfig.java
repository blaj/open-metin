package com.blaj.openmetin.authentication.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class AmqpConfig {

  public static final String EXCHANGE_AUTHENTICATION = "openmetin.authentication.exchange";
  public static final String QUEUE_AUTHENTICATION_CLOSE_CONNECTION =
      "openmetin.authentication.close_connection.queue";
  public static final String ROUTING_AUTHENTICATION_CLOSE_CONNECTION =
      "openmetin.authentication.close_connection.routing";

  @Bean
  public Declarables topicBinding() {
    var authenticationExchange = new TopicExchange(EXCHANGE_AUTHENTICATION, true, false);

    var authenticationCloseConnectionQueue = new Queue(QUEUE_AUTHENTICATION_CLOSE_CONNECTION, true);

    return new Declarables(
        authenticationExchange,
        authenticationCloseConnectionQueue,
        new Binding(
            QUEUE_AUTHENTICATION_CLOSE_CONNECTION,
            DestinationType.QUEUE,
            EXCHANGE_AUTHENTICATION,
            ROUTING_AUTHENTICATION_CLOSE_CONNECTION,
            null));
  }

  @Bean
  public MessageConverter messageConverter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    var rabbitAdmin = new RabbitAdmin(connectionFactory);
    rabbitAdmin.setAutoStartup(true);

    return rabbitAdmin;
  }
}
