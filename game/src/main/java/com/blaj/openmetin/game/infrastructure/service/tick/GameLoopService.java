package com.blaj.openmetin.game.infrastructure.service.tick;

import com.blaj.openmetin.game.infrastructure.properties.GameLoopProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameLoopService implements SmartLifecycle {

  private static final Duration MAX_TICK_TIME = Duration.ofMillis(250);
  private static final int TPS_LOG_INTERVAL = 60;

  private final GameLoopProperties gameLoopProperties;
  private final MapUpdateService mapUpdateService;

  private final AtomicBoolean isRunning = new AtomicBoolean(false);

  private Thread gameLoopThread;
  private long currentTick;
  private Duration accumulator = Duration.ZERO;
  private long lastTpsCheckTick = 0;
  private Instant lastTpsCheckTime;

  @Override
  public void start() {
    if (!gameLoopProperties.enabled()) {
      log.info("Game loop is disabled in configuration");
      return;
    }

    if (isRunning.compareAndSet(false, true)) {
      gameLoopThread = new Thread(this::gameLoop, "GameLoop-Thread");
      gameLoopThread.setDaemon(false);
      gameLoopThread.start();

      log.info(
          "Game loop started - Target TPS: {}, Timestep: {}ms",
          gameLoopProperties.targetTps(),
          gameLoopProperties.fixedTimestepMs());
    }
  }

  @Override
  public void stop() {
    if (isRunning.compareAndSet(true, false)) {
      log.info("Stopping game loop...");

      try {
        if (gameLoopThread != null) {
          gameLoopThread.join(5000);
          log.info("Game loop stopped gracefully at tick {}", currentTick);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("Game loop shutdown interrupted", e);
      }
    }
  }

  @Override
  public boolean isRunning() {
    return isRunning.get();
  }

  @Override
  public int getPhase() {
    return Integer.MAX_VALUE - 100;
  }

  @Override
  public boolean isAutoStartup() {
    return gameLoopProperties.enabled();
  }

  private void gameLoop() {
    var fixedTimestep = Duration.ofMillis(gameLoopProperties.fixedTimestepMs());
    var previousTime = Instant.now();
    lastTpsCheckTime = Instant.now();

    log.info("Game loop thread started");

    while (isRunning.get()) {
      var currentTime = Instant.now();
      var frameTime = Duration.between(previousTime, currentTime);

      if (frameTime.compareTo(MAX_TICK_TIME) > 0) {
        frameTime = MAX_TICK_TIME;
        log.warn("Frame time clamped to max: {}ms", MAX_TICK_TIME.toMillis());
      }

      accumulator = accumulator.plus(frameTime);
      previousTime = currentTime;

      while (accumulator.compareTo(fixedTimestep) >= 0) {
        tick();
        accumulator = accumulator.minus(fixedTimestep);
        currentTick++;

        if (currentTick % TPS_LOG_INTERVAL == 0) {
          logTicksPerSecond();
        }
      }

      sleepBriefly();
    }

    log.info("Game loop thread finished");
  }

  private void tick() {
    mapUpdateService.update();
  }

  private void logTicksPerSecond() {
    var now = Instant.now();
    var ticksSinceLastCheck = currentTick - lastTpsCheckTick;
    var timeSinceLastCheck = Duration.between(lastTpsCheckTime, now);

    var actualTps = ticksSinceLastCheck / (timeSinceLastCheck.toMillis() / 1000.0);

    log.debug(
        "Tick: {} | TPS: {}/{} (actual/target) | {}%",
        currentTick,
        String.format("%.2f", actualTps),
        gameLoopProperties.targetTps(),
        String.format("%.2f", actualTps / gameLoopProperties.targetTps() * 100));

    lastTpsCheckTick = currentTick;
    lastTpsCheckTime = now;
  }

  private void sleepBriefly() {
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      isRunning.set(false);
    }
  }
}
