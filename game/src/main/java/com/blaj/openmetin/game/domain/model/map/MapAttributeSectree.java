package com.blaj.openmetin.game.domain.model.map;

import com.blaj.openmetin.game.domain.enums.map.MapAttribute;

public record MapAttributeSectree(int[] cellAttributes) {

  public static final int CELLS_PER_AXIS = 128; // SECTREE_SIZE / CELL_SIZE = 6400 / 50
  public static final int CELLS_PER_SECTREE = CELLS_PER_AXIS * CELLS_PER_AXIS;

  public static final MapAttributeSectree EMPTY =
      new MapAttributeSectree(new int[CELLS_PER_SECTREE]);

  public MapAttributeSectree {
    if (cellAttributes.length != CELLS_PER_SECTREE) {
      throw new IllegalArgumentException(
          "Invalid sectree size: expected %d, got %d"
              .formatted(CELLS_PER_SECTREE, cellAttributes.length));
    }
  }

  public int get(int x, int y) {
    if (x < 0 || x >= CELLS_PER_AXIS || y < 0 || y >= CELLS_PER_AXIS) {
      return MapAttribute.NONE.getValue();
    }

    return cellAttributes[y * CELLS_PER_AXIS + x];
  }

  public boolean hasAttribute(int x, int y, MapAttribute attribute) {
    var attrs = get(x, y);
    return MapAttribute.hasFlag(attrs, attribute);
  }
}
