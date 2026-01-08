package com.blaj.openmetin.game.infrastructure.runner;

import com.blaj.openmetin.game.infrastructure.service.animation.AnimationProviderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Order(2)
@Service
@RequiredArgsConstructor
@Profile("!test")
public class AnimationInitializerRunner implements ApplicationRunner {

  private final AnimationProviderServiceImpl animationProviderService;

  @Override
  public void run(ApplicationArguments applicationArguments) throws Exception {
    log.info("Starting animations initialization...");
    animationProviderService.loadAnimations();
    log.info("Animations initialized successfully");
  }
}
