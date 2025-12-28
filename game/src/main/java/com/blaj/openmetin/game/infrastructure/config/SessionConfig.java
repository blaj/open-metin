package com.blaj.openmetin.game.infrastructure.config;

import com.blaj.openmetin.game.domain.model.session.GameSession;
import com.blaj.openmetin.shared.common.service.SessionFactoryService;
import com.blaj.openmetin.shared.infrastructure.network.session.SessionManagerServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfig {

  @Bean
  public SessionFactoryService<GameSession> gameSessionSessionFactoryService() {
    return GameSession::new;
  }

  @Bean
  public SessionManagerServiceImpl<GameSession> gameSessionSessionManagerService(
      SessionFactoryService<GameSession> gameSessionSessionFactoryService) {
    return new SessionManagerServiceImpl<>(gameSessionSessionFactoryService);
  }
}
