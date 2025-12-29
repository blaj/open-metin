package com.blaj.openmetin.game.domain.model.map;

import com.blaj.openmetin.game.domain.enums.map.MapAttribute;

public record MapAttributeSet(
    int sectreesWidth,
    int sectreesHeight,
    Coordinates baseCoordinates,
    MapAttributeSectree[][] mapAttributeSectrees) {

  public static final int SECTREE_SIZE = 6400; // 64m x 64m
  public static final int CELL_SIZE = 50; // 50cm x 50cm
  public static final int CELLS_PER_AXIS = SECTREE_SIZE / CELL_SIZE;
  public static final int CELLS_PER_SECTREE = CELLS_PER_AXIS * CELLS_PER_AXIS;

  public int getAttributesAt(Coordinates coordinates) {
    var location = tryLocate(coordinates);

    if (location == null) {
      return MapAttribute.NONE.getValue();
    }

    return location.mapAttributeSectree().get(location.cellX(), location.cellY());
  }

  public boolean hasAttribute(Coordinates coordinates, MapAttribute mapAttribute) {
    var attrs = getAttributesAt(coordinates);
    return MapAttribute.hasFlag(attrs, mapAttribute);
  }

  private SectreeLocation tryLocate(Coordinates coordinates) {
    var relativeCoordinates = coordinates.subtract(baseCoordinates);

    var sectreeIndexX = relativeCoordinates.x() / SECTREE_SIZE;
    var sectreeIndexY = relativeCoordinates.y() / SECTREE_SIZE;

    if (sectreeIndexX >= sectreesWidth || sectreeIndexY >= sectreesHeight) {
      return null;
    }

    var sectree = mapAttributeSectrees[sectreeIndexY][sectreeIndexX];
    if (sectree == null) {
      return null;
    }

    var cellX = (relativeCoordinates.x() % SECTREE_SIZE) / CELL_SIZE;
    var cellY = (relativeCoordinates.y() % SECTREE_SIZE) / CELL_SIZE;

    if (cellX >= CELLS_PER_AXIS || cellY >= CELLS_PER_AXIS) {
      return null;
    }

    return new SectreeLocation(sectree, cellX, cellY);
  }

  private record SectreeLocation(MapAttributeSectree mapAttributeSectree, int cellX, int cellY) {}
}
