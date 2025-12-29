package com.blaj.openmetin.game.domain.model.map;

public record TownCoordinates(
    Coordinates jinno, Coordinates shinsoo, Coordinates chunjo, Coordinates common) {

  public static TownCoordinates allOf(Coordinates coordinates) {
    return new TownCoordinates(coordinates, coordinates, coordinates, coordinates);
  }
}
