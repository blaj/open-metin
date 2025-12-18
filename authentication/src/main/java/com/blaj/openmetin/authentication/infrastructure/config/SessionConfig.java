package com.blaj.openmetin.authentication.infrastructure.config;

import com.blaj.openmetin.shared.common.model.Session;
import com.blaj.openmetin.shared.common.service.SessionFactoryService;
import com.blaj.openmetin.shared.infrastructure.network.session.SessionManagerServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfig {

  @Bean
  public SessionFactoryService<Session> authenticationSessionFactoryService() {
    return Session::new;
  }

  @Bean
  public SessionManagerServiceImpl<Session> authenticationSessionManagerService(
      SessionFactoryService<Session> authenticationSessionFactoryService) {
    return new SessionManagerServiceImpl<>(authenticationSessionFactoryService);
  }
}
