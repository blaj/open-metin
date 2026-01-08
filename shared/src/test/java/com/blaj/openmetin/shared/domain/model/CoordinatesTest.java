package com.blaj.openmetin.shared.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CoordinatesTest {

  @Test
  public void givenNegativeX_whenCreating_thenThrowsException() {
    // given

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(-1, 20));

    // then
    assertThat(thrownException).hasMessage("Coordinates cannot be negative");
  }

  @Test
  public void givenNegativeY_whenCreating_thenThrowsException() {
    // given

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(10, -1));

    // then
    assertThat(thrownException).hasMessage("Coordinates cannot be negative");
  }

  @Test
  public void givenValidCoordinates_whenCreating_thenCreatesSuccessfully() {
    // given

    // when
    var coordinates = new Coordinates(10, 20);

    // then
    assertThat(coordinates.x()).isEqualTo(10);
    assertThat(coordinates.y()).isEqualTo(20);
  }

  @Test
  public void givenCoordinates_whenToString_thenReturnsFormattedString() {
    // given
    var coordinates = new Coordinates(10, 20);

    // when
    var result = coordinates.toString();

    // then
    assertThat(result).isEqualTo("(10, 20)");
  }

  @Test
  public void givenCoordinates_whenMultiply_thenReturnsMultipliedCoordinates() {
    // given
    var coordinates = new Coordinates(10, 20);

    // when
    var result = coordinates.multiply(3);

    // then
    assertThat(result.x()).isEqualTo(30);
    assertThat(result.y()).isEqualTo(60);
  }

  @Test
  public void givenCoordinates_whenMultiplyByZero_thenReturnsZeroCoordinates() {
    // given
    var coordinates = new Coordinates(10, 20);

    // when
    var result = coordinates.multiply(0);

    // then
    assertThat(result.x()).isZero();
    assertThat(result.y()).isZero();
  }

  @Test
  public void givenCoordinates_whenMultiplyOverflow_thenThrowsException() {
    // given
    var coordinates = new Coordinates(Integer.MAX_VALUE, 1);

    // when
    assertThrows(ArithmeticException.class, () -> coordinates.multiply(2));

    // then
  }

  @Test
  public void givenTwoCoordinates_whenAdd_thenReturnsSum() {
    // given
    var first = new Coordinates(10, 20);
    var second = new Coordinates(5, 15);

    // when
    var result = first.add(second);

    // then
    assertThat(result.x()).isEqualTo(15);
    assertThat(result.y()).isEqualTo(35);
  }

  @Test
  public void givenTwoCoordinates_whenAddOverflow_thenThrowsException() {
    // given
    var first = new Coordinates(Integer.MAX_VALUE, 1);
    var second = new Coordinates(1, 1);

    // when
    assertThrows(ArithmeticException.class, () -> first.add(second));

    // then
  }

  @Test
  public void givenCoordinatesAndDeltas_whenAdd_thenReturnsSum() {
    // given
    var coordinates = new Coordinates(10, 20);

    // when
    var result = coordinates.add(5, 15);

    // then
    assertThat(result.x()).isEqualTo(15);
    assertThat(result.y()).isEqualTo(35);
  }

  @Test
  public void givenCoordinatesAndDeltas_whenAddOverflow_thenThrowsException() {
    // given
    var coordinates = new Coordinates(Integer.MAX_VALUE, 1);

    // when
    assertThrows(ArithmeticException.class, () -> coordinates.add(1, 1));

    // then
  }

  @ParameterizedTest
  @CsvSource({
    "100, 200, 50.0, 75.0, 150, 275",
    "100, 200, 0.0, 0.0, 100, 200",
    "100, 200, 0.4, 0.6, 100, 201",
    "100, 200, -50.0, -75.0, 50, 125",
    "100, 200, -100.0, -200.0, 0, 0"
  })
  void givenVector_whenAdd_thenReturnsCorrectCoordinates(
      int startX, int startY, double vectorX, double vectorY, int expectedX, int expectedY) {
    // given
    var coordinates = new Coordinates(startX, startY);
    var vector = new Vector2(vectorX, vectorY);

    // when
    var result = coordinates.add(vector);

    // then
    assertThat(result).isEqualTo(new Coordinates(expectedX, expectedY));
  }

  @Test
  void givenNegativeResult_whenAdd_thenThrowsIllegalArgumentException() {
    // given
    var coordinates = new Coordinates(100, 200);
    var vector = new Vector2(-150.0, -50.0);

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> coordinates.add(vector));

    // then
    assertThat(thrownException).hasMessage("Coordinates cannot be negative");
  }

  @Test
  void givenOverflow_whenAdd_thenThrowsArithmeticException() {
    // given
    var coordinates = new Coordinates(Integer.MAX_VALUE, 100);
    var vector = new Vector2(1.0, 0.0);

    // when
    var thrownException = assertThrows(ArithmeticException.class, () -> coordinates.add(vector));

    // then
  }

  @Test
  public void givenTwoCoordinates_whenSubtract_thenReturnsDifference() {
    // given
    var first = new Coordinates(20, 30);
    var second = new Coordinates(5, 10);

    // when
    var result = first.subtract(second);

    // then
    assertThat(result.x()).isEqualTo(15);
    assertThat(result.y()).isEqualTo(20);
  }

  @Test
  public void givenTwoCoordinates_whenSubtractResultsInNegative_thenThrowsException() {
    // given
    var first = new Coordinates(5, 10);
    var second = new Coordinates(10, 5);

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> first.subtract(second));

    // then
    assertThat(thrownException).hasMessage("Coordinates cannot be negative");
  }
}
