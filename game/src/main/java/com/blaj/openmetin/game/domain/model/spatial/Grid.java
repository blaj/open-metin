package com.blaj.openmetin.game.domain.model.spatial;

import com.blaj.openmetin.game.domain.model.map.Coordinates;
import java.lang.reflect.Array;
import java.util.Optional;
import lombok.Getter;

@Getter
public class Grid<T> {

  private int width;
  private int height;
  private T[][] grid;
  private final Class<T> tClass;

  @SuppressWarnings("unchecked")
  public Grid(Class<T> tClass, int width, int height) {
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Width and height must be positive");
    }

    this.width = width;
    this.height = height;
    this.grid = (T[][]) Array.newInstance(tClass, width, height);
    this.tClass = tClass;
  }

  @SuppressWarnings("unchecked")
  public void resize(int width, int height) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Width and height must be positive");
    }

    this.width = width;
    this.height = height;
    this.grid = (T[][]) Array.newInstance(tClass, width, height);
  }

  public Optional<T> get(int x, int y) {
    try {
      return Optional.of(grid[x][y]);
    } catch (ArrayIndexOutOfBoundsException _) {
      return Optional.empty();
    }
  }

  public void set(int x, int y, T value) {
    try {
      grid[x][y] = value;
    } catch (ArrayIndexOutOfBoundsException _) {

    }
  }

  public Optional<Coordinates> getFreeCoordinates(int requiredWidth, int requiredHeight) {
    if (requiredWidth <= 0 || requiredHeight <= 0) {
      throw new IllegalArgumentException("Required width and height must be positive");
    }

    for (var y = 0; y <= height - requiredHeight; y++) {
      for (var x = 0; x <= width - requiredWidth; x++) {
        if (get(x, y).isPresent()) {
          continue;
        }

        if (isAreaFree(x, y, requiredWidth, requiredHeight)) {
          return Optional.of(new Coordinates(x, y));
        }
      }
    }

    return Optional.empty();
  }

  private boolean isAreaFree(int startX, int startY, int areaWidth, int areaHeight) {
    for (var y = startY; y < startY + areaHeight; y++) {
      for (var x = startX; x < startX + areaWidth; x++) {
        if (get(x, y).isPresent()) {
          return false;
        }
      }
    }
    return true;
  }
}
