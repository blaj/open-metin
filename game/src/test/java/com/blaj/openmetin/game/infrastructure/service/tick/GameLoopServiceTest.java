package com.blaj.openmetin.game.infrastructure.service.tick;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeast;

import com.blaj.openmetin.game.infrastructure.properties.GameLoopProperties;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GameLoopServiceTest {

  private GameLoopService gameLoopService;

  @Mock private GameLoopProperties gameLoopProperties;
  @Mock private MapUpdateService mapUpdateService;

  @BeforeEach
  public void beforeEach() {
    gameLoopService = new GameLoopService(gameLoopProperties, mapUpdateService);
  }

  @Test
  public void givenGameLoopDisabled_whenStart_thenDoNotStartThread() {
    // given
    given(gameLoopProperties.enabled()).willReturn(false);

    // when
    gameLoopService.start();

    // then
    assertThat(gameLoopService.isRunning()).isFalse();
  }

  @Test
  public void givenGameLoopEnabled_whenStart_thenStartThread() {
    // given
    given(gameLoopProperties.enabled()).willReturn(true);
    given(gameLoopProperties.targetTps()).willReturn(25);
    given(gameLoopProperties.fixedTimestepMs()).willReturn(40);

    // when
    gameLoopService.start();

    // then
    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              assertThat(gameLoopService.isRunning()).isTrue();
              then(mapUpdateService).should(atLeast(1)).update();
            });

    // cleanup
    gameLoopService.stop();
  }

  @Test
  public void givenAlreadyRunning_whenStart_thenDoNotStartAgain() {
    // given
    given(gameLoopProperties.enabled()).willReturn(true);
    given(gameLoopProperties.targetTps()).willReturn(25);
    given(gameLoopProperties.fixedTimestepMs()).willReturn(40);

    gameLoopService.start();

    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              assertThat(gameLoopService.isRunning()).isTrue();
            });

    // when
    gameLoopService.start();

    // then
    assertThat(gameLoopService.isRunning()).isTrue();

    // cleanup
    gameLoopService.stop();
  }

  @Test
  public void givenNotRunning_whenStop_thenDoNothing() {
    // given

    // when
    gameLoopService.stop();

    // then
    assertThat(gameLoopService.isRunning()).isFalse();
  }

  @Test
  public void givenRunning_whenStop_thenStopThread() {
    // given
    given(gameLoopProperties.enabled()).willReturn(true);
    given(gameLoopProperties.targetTps()).willReturn(25);
    given(gameLoopProperties.fixedTimestepMs()).willReturn(40);

    gameLoopService.start();

    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              assertThat(gameLoopService.isRunning()).isTrue();
            });

    // when
    gameLoopService.stop();

    // then
    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              assertThat(gameLoopService.isRunning()).isFalse();
            });
  }

  @Test
  public void givenAlreadyStopped_whenStop_thenDoNothing() {
    // given
    given(gameLoopProperties.enabled()).willReturn(true);
    given(gameLoopProperties.targetTps()).willReturn(25);
    given(gameLoopProperties.fixedTimestepMs()).willReturn(40);

    gameLoopService.start();

    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              assertThat(gameLoopService.isRunning()).isTrue();
            });

    gameLoopService.stop();

    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              assertThat(gameLoopService.isRunning()).isFalse();
            });

    // when
    gameLoopService.stop();

    // then
    assertThat(gameLoopService.isRunning()).isFalse();
  }

  @Test
  public void givenNotStarted_whenIsRunning_thenReturnFalse() {
    // when
    var result = gameLoopService.isRunning();

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenStarted_whenIsRunning_thenReturnTrue() {
    // given
    given(gameLoopProperties.enabled()).willReturn(true);
    given(gameLoopProperties.targetTps()).willReturn(25);
    given(gameLoopProperties.fixedTimestepMs()).willReturn(40);

    gameLoopService.start();

    // when & then
    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              assertThat(gameLoopService.isRunning()).isTrue();
            });

    // cleanup
    gameLoopService.stop();
  }

  @Test
  public void whenGetPhase_thenReturnHighPriority() {
    // when
    var result = gameLoopService.getPhase();

    // then
    assertThat(result).isEqualTo(Integer.MAX_VALUE - 100);
  }

  @Test
  public void givenGameLoopDisabled_whenIsAutoStartup_thenReturnFalse() {
    // given
    given(gameLoopProperties.enabled()).willReturn(false);

    // when
    var result = gameLoopService.isAutoStartup();

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenGameLoopEnabled_whenIsAutoStartup_thenReturnTrue() {
    // given
    given(gameLoopProperties.enabled()).willReturn(true);

    // when
    var result = gameLoopService.isAutoStartup();

    // then
    assertThat(result).isTrue();
  }

  // Test integracyjny

  @Test
  public void givenGameLoop_whenStartAndStop_thenExecuteTicksAndStopGracefully() {
    // given
    given(gameLoopProperties.enabled()).willReturn(true);
    given(gameLoopProperties.targetTps()).willReturn(25);
    given(gameLoopProperties.fixedTimestepMs()).willReturn(40);

    // when
    gameLoopService.start();

    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              assertThat(gameLoopService.isRunning()).isTrue();
              then(mapUpdateService).should(atLeast(5)).update();
            });

    gameLoopService.stop();

    // then
    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              assertThat(gameLoopService.isRunning()).isFalse();
            });
  }
}
