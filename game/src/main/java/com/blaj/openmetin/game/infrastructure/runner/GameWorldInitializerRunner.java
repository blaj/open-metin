package com.blaj.openmetin.game.infrastructure.runner;

import com.blaj.openmetin.game.infrastructure.service.world.GameWorldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Order(1)
@Service
@RequiredArgsConstructor
public class GameWorldInitializerRunner implements ApplicationRunner {

  private final GameWorldService gameWorldService;

  @Override
  public void run(ApplicationArguments applicationArguments) {
    log.info("Starting game world initialization...");
    gameWorldService.loadMaps();
    log.info("Game world initialized successfully");
  }
}
