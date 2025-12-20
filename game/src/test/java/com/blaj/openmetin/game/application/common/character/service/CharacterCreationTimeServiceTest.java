package com.blaj.openmetin.game.application.common.character.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
public class CharacterCreationTimeServiceTest {

  private CharacterCreationTimeService characterCreationTimeService;

  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private Clock clock;
  @Mock private Instant instant;

  @BeforeEach
  public void beforeEach() {
    characterCreationTimeService = new CharacterCreationTimeService(redisTemplate, clock);

    given(clock.instant()).willReturn(instant);
  }

  @Test
  public void givenNullResult_whenTryConsume_thenReturnFalse() {
    // given
    var accountId = 123L;
    var window = Duration.ofSeconds(30);
    var epochSecond = 333L;

    given(instant.getEpochSecond()).willReturn(epochSecond);
    given(
            redisTemplate.execute(
                any(),
                eq(List.of("account:" + accountId + ":character_create")),
                eq(epochSecond),
                eq(window.getSeconds())))
        .willReturn(null);

    // when
    var result = characterCreationTimeService.tryConsume(accountId, window);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenResultGreatherThan1_whenTryConsume_thenReturnTrue() {
    // given
    var accountId = 123L;
    var window = Duration.ofSeconds(30);
    var epochSecond = 333L;

    given(instant.getEpochSecond()).willReturn(epochSecond);

    given(
        redisTemplate.execute(
            any(),
            eq(List.of("account:" + accountId + ":character_create")),
            eq(epochSecond),
            eq(window.getSeconds())))
        .willReturn(2L);

    // when
    var result = characterCreationTimeService.tryConsume(accountId, window);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenValid_whenTryConsume_thenReturnTrue() {
    // given
    var accountId = 123L;
    var window = Duration.ofSeconds(30);
    var epochSecond = 333L;

    given(instant.getEpochSecond()).willReturn(epochSecond);

    given(
        redisTemplate.execute(
            any(),
            eq(List.of("account:" + accountId + ":character_create")),
            eq(epochSecond),
            eq(window.getSeconds())))
        .willReturn(1L);

    // when
    var result = characterCreationTimeService.tryConsume(accountId, window);

    // then
    assertThat(result).isTrue();
  }
}
