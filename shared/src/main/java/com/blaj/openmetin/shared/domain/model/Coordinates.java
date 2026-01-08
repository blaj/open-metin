package com.blaj.openmetin.shared.domain.model;

public record Coordinates(int x, int y) {

  public Coordinates {
    if (x < 0 || y < 0) {
      throw new IllegalArgumentException("Coordinates cannot be negative");
    }
  }

  @Override
  public String toString() {
    return "(%d, %d)".formatted(x, y);
  }

  public Coordinates multiply(int multiplier) {
    return new Coordinates(Math.multiplyExact(x, multiplier), Math.multiplyExact(y, multiplier));
  }

  public Coordinates add(Coordinates other) {
    return new Coordinates(Math.addExact(x, other.x), Math.addExact(y, other.y));
  }

  public Coordinates add(int deltaX, int deltaY) {
    return new Coordinates(Math.addExact(x, deltaX), Math.addExact(y, deltaY));
  }

  public Coordinates add(Vector2 vector) {
    int deltaX = (int) Math.round(vector.x());
    int deltaY = (int) Math.round(vector.y());

    return new Coordinates(Math.addExact(x, deltaX), Math.addExact(y, deltaY));
  }

  public Coordinates subtract(Coordinates other) {
    return new Coordinates(Math.subtractExact(x, other.x), Math.subtractExact(y, other.y));
  }
}
