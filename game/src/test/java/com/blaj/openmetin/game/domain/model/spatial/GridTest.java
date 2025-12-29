package com.blaj.openmetin.game.domain.model.spatial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class GridTest {

  @Test
  public void givenNegativeWidth_whenCreateGrid_thenThrowException() {
    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> new Grid<>(String.class, -1, 10));

    // then
    assertThat(thrownException).hasMessageContaining("Width and height must be positive");
  }

  @Test
  public void givenNegativeHeight_whenCreateGrid_thenThrowException() {
    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> new Grid<>(String.class, 10, -1));

    // then
    assertThat(thrownException).hasMessageContaining("Width and height must be positive");
  }

  @Test
  public void givenZeroDimensions_whenCreateGrid_thenCreateEmptyGrid() {
    // when
    var grid = new Grid<>(String.class, 0, 0);

    // then
    assertThat(grid.getWidth()).isEqualTo(0);
    assertThat(grid.getHeight()).isEqualTo(0);
  }

  @Test
  public void givenValidDimensions_whenCreateGrid_thenCreateGrid() {
    // when
    var grid = new Grid<>(String.class, 10, 20);

    // then
    assertThat(grid.getWidth()).isEqualTo(10);
    assertThat(grid.getHeight()).isEqualTo(20);
  }

  @Test
  public void givenZeroWidth_whenResize_thenThrowException() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var thrownException = assertThrows(IllegalArgumentException.class, () -> grid.resize(0, 10));

    // then
    assertThat(thrownException).hasMessageContaining("Width and height must be positive");
  }

  @Test
  public void givenZeroHeight_whenResize_thenThrowException() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var thrownException = assertThrows(IllegalArgumentException.class, () -> grid.resize(10, 0));

    // then
    assertThat(thrownException).hasMessageContaining("Width and height must be positive");
  }

  @Test
  public void givenNegativeWidth_whenResize_thenThrowException() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var thrownException = assertThrows(IllegalArgumentException.class, () -> grid.resize(-1, 10));

    // then
    assertThat(thrownException).hasMessageContaining("Width and height must be positive");
  }

  @Test
  public void givenNegativeHeight_whenResize_thenThrowException() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var thrownException = assertThrows(IllegalArgumentException.class, () -> grid.resize(10, -1));

    // then
    assertThat(thrownException).hasMessageContaining("Width and height must be positive");
  }

  @Test
  public void givenValidDimensions_whenResize_thenResizeGrid() {
    // given
    var grid = new Grid<>(String.class, 10, 10);
    grid.set(5, 5, "value");

    // when
    grid.resize(20, 30);

    // then
    assertThat(grid.getWidth()).isEqualTo(20);
    assertThat(grid.getHeight()).isEqualTo(30);
    assertThat(grid.get(5, 5)).isEmpty(); // Dane powinny zostać wyczyszczone
  }

  @Test
  public void givenOutOfBoundsCoordinates_whenGet_thenReturnEmpty() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var result = grid.get(15, 5);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenNegativeCoordinates_whenGet_thenReturnEmpty() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var result = grid.get(-1, 5);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenEmptyCell_whenGet_thenReturnEmpty() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var result = grid.get(5, 5);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenSetValue_whenGet_thenReturnValue() {
    // given
    var grid = new Grid<>(String.class, 10, 10);
    var value = "test";
    grid.set(5, 5, value);

    // when
    var result = grid.get(5, 5);

    // then
    assertThat(result).contains(value);
  }

  @Test
  public void givenOutOfBoundsCoordinates_whenSet_thenIgnore() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    grid.set(15, 5, "value");

    // then
    assertThat(grid.get(15, 5)).isEmpty();
  }

  @Test
  public void givenNegativeCoordinates_whenSet_thenIgnore() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    grid.set(-1, 5, "value");

    // then
    assertThat(grid.get(-1, 5)).isEmpty();
  }

  @Test
  public void givenValidCoordinates_whenSet_thenSetValue() {
    // given
    var grid = new Grid<>(String.class, 10, 10);
    var value = "test";

    // when
    grid.set(5, 5, value);

    // then
    assertThat(grid.get(5, 5)).contains(value);
  }

  @Test
  public void givenExistingValue_whenSet_thenOverwriteValue() {
    // given
    var grid = new Grid<>(String.class, 10, 10);
    grid.set(5, 5, "old");
    var newValue = "new";

    // when
    grid.set(5, 5, newValue);

    // then
    assertThat(grid.get(5, 5)).contains(newValue);
  }

  @Test
  public void givenZeroRequiredWidth_whenGetFreeCoordinates_thenThrowException() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> grid.getFreeCoordinates(0, 5));

    // then
    assertThat(thrownException).hasMessageContaining("Required width and height must be positive");
  }

  @Test
  public void givenZeroRequiredHeight_whenGetFreeCoordinates_thenThrowException() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> grid.getFreeCoordinates(5, 0));

    // then
    assertThat(thrownException).hasMessageContaining("Required width and height must be positive");
  }

  @Test
  public void givenNegativeRequiredWidth_whenGetFreeCoordinates_thenThrowException() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> grid.getFreeCoordinates(-1, 5));

    // then
    assertThat(thrownException).hasMessageContaining("Required width and height must be positive");
  }

  @Test
  public void givenNegativeRequiredHeight_whenGetFreeCoordinates_thenThrowException() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> grid.getFreeCoordinates(5, -1));

    // then
    assertThat(thrownException).hasMessageContaining("Required width and height must be positive");
  }

  @Test
  public void givenFullGrid_whenGetFreeCoordinates_thenReturnEmpty() {
    // given
    var grid = new Grid<>(String.class, 10, 10);
    for (int x = 0; x < 10; x++) {
      for (int y = 0; y < 10; y++) {
        grid.set(x, y, "occupied");
      }
    }

    // when
    var result = grid.getFreeCoordinates(2, 2);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenRequiredSizeLargerThanGrid_whenGetFreeCoordinates_thenReturnEmpty() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var result = grid.getFreeCoordinates(15, 15);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  public void givenEmptyGrid_whenGetFreeCoordinates_thenReturnFirstCoordinates() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    // when
    var result = grid.getFreeCoordinates(2, 2);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result.get().x()).isEqualTo(0);
    assertThat(result.get().y()).isEqualTo(0);
  }

  @Test
  public void givenGridWithFreeSpace_whenGetFreeCoordinates_thenReturnFreeCoordinates() {
    // given
    var grid = new Grid<>(String.class, 10, 10);
    grid.set(0, 0, "occupied");
    grid.set(1, 0, "occupied");

    // when
    var result = grid.getFreeCoordinates(2, 2);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result.get().x()).isGreaterThanOrEqualTo(0);
    assertThat(result.get().y()).isGreaterThanOrEqualTo(0);
  }

  @Test
  public void givenPartiallyOccupiedGrid_whenGetFreeCoordinates_thenReturnEmpty() {
    // given
    var grid = new Grid<>(String.class, 10, 10);

    for (var x = 0; x < 10; x++) {
      for (var y = 0; y < 10; y++) {
        if (x % 2 == 0 || y % 2 == 0) {
          grid.set(x, y, "occupied");
        }
      }
    }

    // when
    var result = grid.getFreeCoordinates(2, 2);

    // then
    assertThat(result).isEmpty();
  }
}
