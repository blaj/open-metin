package com.blaj.openmetin.shared.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class DateTimeUtilsTest {

  @AfterEach
  public void afterEach() throws Exception {
    resetBootTime();
  }

  @Test
  public void givenNotInitialized_whenGetBootTime_thenThrowsException() {
    // given

    // when
    var thrownException = assertThrows(IllegalStateException.class, DateTimeUtils::getBootTime);

    // then
    assertThat(thrownException).hasMessageContaining("DateTimeUtils not initialized");
  }

  @Test
  public void givenInitialized_whenGetBootTime_thenReturnsBootTime() {
    // given
    DateTimeUtils.initialize();

    // when
    var result = DateTimeUtils.getBootTime();

    // then
    assertThat(result).isPositive();
  }

  @Test
  public void givenCalledMultipleTimes_whenInitialize_thenBootTimeStaysTheSame() throws Exception {
    // given
    DateTimeUtils.initialize();

    // when
    var firstBootTime = DateTimeUtils.getBootTime();
    Thread.sleep(10);
    DateTimeUtils.initialize();
    var secondBootTime = DateTimeUtils.getBootTime();

    // then
    assertThat(firstBootTime).isEqualTo(secondBootTime);
  }

  @Test
  public void givenInitialized_whenGetUnixTime_thenReturnsTime() {
    // given
    DateTimeUtils.initialize();

    // when
    var result = DateTimeUtils.getUnixTime();

    // then
    assertThat(result).isPositive();
  }

  @Test
  public void givenNotInitialized_whenGetUnixTime_thenThrowsException() {
    // given

    // when
    var thrownException = assertThrows(IllegalStateException.class, DateTimeUtils::getUnixTime);

    // then
    assertThat(thrownException).hasMessageContaining("DateTimeUtils not initialized");
  }

  @Test
  public void whenGetGlobalTime_thenReturnsCurrentTime() {
    // given

    // when
    var before = Instant.now().getEpochSecond();
    var result = DateTimeUtils.getGlobalTime();
    var after = Instant.now().getEpochSecond();

    // then
    assertThat(result).isBetween(before, after);
  }

  @Test
  public void givenInitialized_whenGetUnixTime_thenReturnsMillisecondsSinceBootTime()
      throws Exception {
    // given
    DateTimeUtils.initialize();
    Thread.sleep(100);

    // when
    var unixTime = DateTimeUtils.getUnixTime();

    // then
    assertThat(unixTime).isGreaterThanOrEqualTo(100);
  }

  private void resetBootTime() throws Exception {
    var bootTimeField = DateTimeUtils.class.getDeclaredField("bootTime");
    bootTimeField.setAccessible(true);
    bootTimeField.set(null, null);
  }
}
