package com.blaj.openmetin.shared.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

public class MathUtilsTest {

  @Test
  public void givenSamePoint_whenDistance_thenReturn0() {
    // given

    // when
    var result = MathUtils.distance(100, 100, 100, 100);

    // then
    assertThat(result).isCloseTo(0.0, within(0.001));
  }

  @Test
  public void givenPointsOnXAxis_whenDistance_thenReturnHorizontalDistance() {
    // given

    // when
    var result = MathUtils.distance(0, 0, 10, 0);

    // then
    assertThat(result).isCloseTo(10.0, within(0.001));
  }

  @Test
  public void givenPointsOnYAxis_whenDistance_thenReturnVerticalDistance() {
    // given

    // when
    var result = MathUtils.distance(0, 0, 0, 10);

    // then
    assertThat(result).isCloseTo(10.0, within(0.001));
  }

  @Test
  public void givenPointsOnDiagonal_whenDistance_thenReturnDiagonalDistance() {
    // given

    // when
    var result = MathUtils.distance(0, 0, 3, 4);

    // then
    assertThat(result).isCloseTo(5.0, within(0.001));
  }

  @Test
  public void givenNegativeCoordinates_whenDistance_thenReturnCorrectDistance() {
    // given

    // when
    var result = MathUtils.distance(-3, -4, 0, 0);

    // then
    assertThat(result).isCloseTo(5.0, within(0.001));
  }

  @Test
  public void givenTypicalPoints_whenDistance_thenReturnCorrectDistance() {
    // given

    // when
    var result = MathUtils.distance(100, 200, 400, 600);

    // then
    assertThat(result).isCloseTo(500.0, within(0.001));
  }

  @Test
  public void givenZeroVector_whenRotation_thenReturn0() {
    // given

    // when
    var result = MathUtils.rotation(0, 0);

    // then
    assertThat(result).isCloseTo(0.0, within(0.001));
  }

  @Test
  public void givenVectorPointingUp_whenRotation_thenReturn0() {
    // given

    // when
    var result = MathUtils.rotation(0, 1);

    // then
    assertThat(result).isCloseTo(0.0, within(0.001));
  }

  @Test
  public void givenVectorPointingRight_whenRotation_thenReturn90() {
    // given

    // when
    var result = MathUtils.rotation(1, 0);

    // then
    assertThat(result).isCloseTo(90.0, within(0.001));
  }

  @Test
  public void givenVectorPointingDown_whenRotation_thenReturn180() {
    // given

    // when
    var result = MathUtils.rotation(0, -1);

    // then
    assertThat(result).isCloseTo(180.0, within(0.001));
  }

  @Test
  public void givenVectorPointingLeft_whenRotation_thenReturn270() {
    // given

    // when
    var result = MathUtils.rotation(-1, 0);

    // then
    assertThat(result).isCloseTo(270.0, within(0.001));
  }

  @Test
  public void givenVectorPointingTopRight_whenRotation_thenReturn45() {
    // given

    // when
    var result = MathUtils.rotation(1, 1);

    // then
    assertThat(result).isCloseTo(45.0, within(0.001));
  }

  @Test
  public void givenVectorPointingTopLeft_whenRotation_thenReturn315() {
    // given

    // when
    var result = MathUtils.rotation(-1, 1);

    // then
    assertThat(result).isCloseTo(315.0, within(0.001));
  }

  @Test
  public void givenVectorPointingBottomRight_whenRotation_thenReturn135() {
    // given

    // when
    var result = MathUtils.rotation(1, -1);

    // then
    assertThat(result).isCloseTo(135.0, within(0.001));
  }

  @Test
  public void givenVectorPointingBottomLeft_whenRotation_thenReturn225() {
    // given

    // when
    var result = MathUtils.rotation(-1, -1);

    // then
    assertThat(result).isCloseTo(225.0, within(0.001));
  }
}
