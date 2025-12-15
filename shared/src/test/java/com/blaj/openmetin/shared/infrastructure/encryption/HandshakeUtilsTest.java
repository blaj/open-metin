package com.blaj.openmetin.shared.infrastructure.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class HandshakeUtilsTest {
  @RepeatedTest(100)
  public void whenGenerateUInt32_thenReturnsValueInValidRange() {
    // given

    // when
    var result = HandshakeUtils.generateUInt32();

    // then
    assertThat(result).isBetween(0L, 0xFFFFFFFFL);
  }

  @RepeatedTest(100)
  public void givenPercentage100_whenPercentageCheck_thenAlwaysReturnsTrue() {
    // given

    // when
    var result = HandshakeUtils.percentageCheck(100);

    // then
    assertThat(result).isTrue();
  }

  @RepeatedTest(100)
  public void givenPercentage0_whenPercentageCheck_thenAlwaysReturnsFalse() {
    // given

    // when
    var result = HandshakeUtils.percentageCheck(0);

    // then
    assertThat(result).isFalse();
  }

  @RepeatedTest(100)
  public void givenValidRange_whenGenerateInt32_thenReturnsValueInRange() {
    // given

    // when
    var result = HandshakeUtils.generateInt32(1, 10);

    // then
    assertThat(result).isBetween(1, 9);
  }

  @Test
  public void givenFromEqualsTo_whenGenerateInt32_thenThrowsException() {
    // given

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> HandshakeUtils.generateInt32(5, 5));

    // then
    assertThat(thrownException)
        .hasMessageContaining("fromInclusive must be smaller than toExclusive");
  }

  @Test
  public void givenFromGreaterThanTo_whenGenerateInt32_thenThrowsException() {
    // given

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> HandshakeUtils.generateInt32(10, 5));

    // then
    assertThat(thrownException)
        .hasMessageContaining("fromInclusive must be smaller than toExclusive");
  }

  @RepeatedTest(100)
  public void givenValidRange_whenGenerateUInt32WithRange_thenReturnsValueInRange() {
    // given

    // when
    var result = HandshakeUtils.generateUInt32(1L, 100L);

    // then
    assertThat(result).isBetween(1L, 99L);
  }

  @Test
  public void givenFromEqualsTo_whenGenerateUInt32WithRange_thenThrowsException() {
    // given

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> HandshakeUtils.generateUInt32(5L, 5L));

    // then
    assertThat(thrownException)
        .hasMessageContaining("fromInclusive must be smaller than toExclusive");
  }

  @Test
  public void givenFromGreaterThanTo_whenGenerateUInt32WithRange_thenThrowsException() {
    // given

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> HandshakeUtils.generateUInt32(10L, 5L));

    // then
    assertThat(thrownException)
        .hasMessageContaining("fromInclusive must be smaller than toExclusive");
  }

  @Test
  public void givenRangeOfOne_whenGenerateUInt32WithRange_thenReturnsFromInclusive() {
    // given

    // when
    var result = HandshakeUtils.generateUInt32(5L, 6L);

    // then
    assertThat(result).isEqualTo(5L);
  }

  @RepeatedTest(100)
  public void givenLargeRange_whenGenerateUInt32WithRange_thenReturnsValueInRange() {
    // given

    // when
    var result = HandshakeUtils.generateUInt32(0L, 0xFFFFFFFFL);

    // then
    assertThat(result).isBetween(0L, 0xFFFFFFFEL);
  }
}
