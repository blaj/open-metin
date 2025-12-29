package com.blaj.openmetin.game.domain.model.map;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.domain.enums.map.MapAttribute;
import org.junit.jupiter.api.Test;

public class MapAttributeSetTest {

  @Test
  public void givenCoordinatesOutsideWidth_whenGetAttributesAt_thenReturnNone() {
    // given
    var baseCoordinates = new Coordinates(0, 0);
    var sectrees = new MapAttributeSectree[2][2];
    sectrees[0][0] = MapAttributeSectree.EMPTY;
    var mapAttributeSet = new MapAttributeSet(2, 2, baseCoordinates, sectrees);

    var coordinates = new Coordinates(2 * MapAttributeSet.SECTREE_SIZE + 100, 0);

    // when
    var result = mapAttributeSet.getAttributesAt(coordinates);

    // then
    assertThat(result).isEqualTo(MapAttribute.NONE.getValue());
  }

  @Test
  public void givenCoordinatesOutsideHeight_whenGetAttributesAt_thenReturnNone() {
    // given
    var baseCoordinates = new Coordinates(0, 0);
    var sectrees = new MapAttributeSectree[2][2];
    sectrees[0][0] = MapAttributeSectree.EMPTY;
    var mapAttributeSet = new MapAttributeSet(2, 2, baseCoordinates, sectrees);

    var coordinates = new Coordinates(0, 2 * MapAttributeSet.SECTREE_SIZE + 100);

    // when
    var result = mapAttributeSet.getAttributesAt(coordinates);

    // then
    assertThat(result).isEqualTo(MapAttribute.NONE.getValue());
  }

  @Test
  public void givenNullSectree_whenGetAttributesAt_thenReturnNone() {
    // given
    var baseCoordinates = new Coordinates(0, 0);
    var sectrees = new MapAttributeSectree[2][2];
    sectrees[0][0] = null;
    var mapAttributeSet = new MapAttributeSet(2, 2, baseCoordinates, sectrees);

    var coordinates = new Coordinates(100, 100);

    // when
    var result = mapAttributeSet.getAttributesAt(coordinates);

    // then
    assertThat(result).isEqualTo(MapAttribute.NONE.getValue());
  }

  @Test
  public void givenValidCoordinates_whenGetAttributesAt_thenReturnValue() {
    // given
    var baseCoordinates = new Coordinates(0, 0);
    var array = new int[MapAttributeSet.CELLS_PER_SECTREE];
    array[10 * MapAttributeSet.CELLS_PER_AXIS + 5] = MapAttribute.BLOCK.getValue();
    var sectree = new MapAttributeSectree(array);
    var sectrees = new MapAttributeSectree[1][1];
    sectrees[0][0] = sectree;
    var mapAttributeSet = new MapAttributeSet(1, 1, baseCoordinates, sectrees);

    var coordinates =
        new Coordinates(5 * MapAttributeSet.CELL_SIZE, 10 * MapAttributeSet.CELL_SIZE);

    // when
    var result = mapAttributeSet.getAttributesAt(coordinates);

    // then
    assertThat(result).isEqualTo(MapAttribute.BLOCK.getValue());
  }

  @Test
  public void givenCoordinatesWithBaseOffset_whenGetAttributesAt_thenCalculateCorrectly() {
    // given
    var baseCoordinates = new Coordinates(1000, 2000);
    var array = new int[MapAttributeSet.CELLS_PER_SECTREE];
    array[10 * MapAttributeSet.CELLS_PER_AXIS + 5] = MapAttribute.WATER.getValue();
    var sectree = new MapAttributeSectree(array);
    var sectrees = new MapAttributeSectree[1][1];
    sectrees[0][0] = sectree;
    var mapAttributeSet = new MapAttributeSet(1, 1, baseCoordinates, sectrees);

    var coordinates =
        new Coordinates(
            1000 + 5 * MapAttributeSet.CELL_SIZE, 2000 + 10 * MapAttributeSet.CELL_SIZE);

    // when
    var result = mapAttributeSet.getAttributesAt(coordinates);

    // then
    assertThat(result).isEqualTo(MapAttribute.WATER.getValue());
  }

  @Test
  public void givenCoordinatesInDifferentSectrees_whenGetAttributesAt_thenReturnCorrectValues() {
    // given
    var baseCoordinates = new Coordinates(0, 0);
    var array1 = new int[MapAttributeSet.CELLS_PER_SECTREE];
    array1[0] = MapAttribute.BLOCK.getValue();
    var sectree1 = new MapAttributeSectree(array1);

    var array2 = new int[MapAttributeSet.CELLS_PER_SECTREE];
    array2[0] = MapAttribute.WATER.getValue();
    var sectree2 = new MapAttributeSectree(array2);

    var sectrees = new MapAttributeSectree[2][2];
    sectrees[0][0] = sectree1;
    sectrees[0][1] = sectree2;
    var mapAttributeSet = new MapAttributeSet(2, 2, baseCoordinates, sectrees);

    // when & then
    assertThat(mapAttributeSet.getAttributesAt(new Coordinates(0, 0)))
        .isEqualTo(MapAttribute.BLOCK.getValue());
    assertThat(mapAttributeSet.getAttributesAt(new Coordinates(MapAttributeSet.SECTREE_SIZE, 0)))
        .isEqualTo(MapAttribute.WATER.getValue());
  }

  @Test
  public void givenCoordinatesAtSectreeBoundary_whenGetAttributesAt_thenReturnCorrectValue() {
    // given
    var baseCoordinates = new Coordinates(0, 0);
    var array = new int[MapAttributeSet.CELLS_PER_SECTREE];
    array[0] = MapAttribute.SAFE.getValue();
    var sectree = new MapAttributeSectree(array);
    var sectrees = new MapAttributeSectree[1][1];
    sectrees[0][0] = sectree;
    var mapAttributeSet = new MapAttributeSet(1, 1, baseCoordinates, sectrees);

    var coordinates = new Coordinates(0, 0);

    // when
    var result = mapAttributeSet.getAttributesAt(coordinates);

    // then
    assertThat(result).isEqualTo(MapAttribute.SAFE.getValue());
  }

  @Test
  public void givenCoordinatesOutsideMap_whenHasAttribute_thenReturnFalse() {
    // given
    var baseCoordinates = new Coordinates(0, 0);
    var sectrees = new MapAttributeSectree[1][1];
    sectrees[0][0] = MapAttributeSectree.EMPTY;
    var mapAttributeSet = new MapAttributeSet(1, 1, baseCoordinates, sectrees);

    var coordinates = new Coordinates(MapAttributeSet.SECTREE_SIZE + 100, 0);

    // when
    var result = mapAttributeSet.hasAttribute(coordinates, MapAttribute.BLOCK);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenAttributeNotPresent_whenHasAttribute_thenReturnFalse() {
    // given
    var baseCoordinates = new Coordinates(0, 0);
    var array = new int[MapAttributeSet.CELLS_PER_SECTREE];
    array[0] = MapAttribute.WATER.getValue();
    var sectree = new MapAttributeSectree(array);
    var sectrees = new MapAttributeSectree[1][1];
    sectrees[0][0] = sectree;
    var mapAttributeSet = new MapAttributeSet(1, 1, baseCoordinates, sectrees);

    var coordinates = new Coordinates(0, 0);

    // when
    var result = mapAttributeSet.hasAttribute(coordinates, MapAttribute.BLOCK);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenAttributePresent_whenHasAttribute_thenReturnTrue() {
    // given
    var baseCoordinates = new Coordinates(0, 0);
    var array = new int[MapAttributeSet.CELLS_PER_SECTREE];
    array[10 * MapAttributeSet.CELLS_PER_AXIS + 5] = MapAttribute.BLOCK.getValue();
    var sectree = new MapAttributeSectree(array);
    var sectrees = new MapAttributeSectree[1][1];
    sectrees[0][0] = sectree;
    var mapAttributeSet = new MapAttributeSet(1, 1, baseCoordinates, sectrees);

    var coordinates =
        new Coordinates(5 * MapAttributeSet.CELL_SIZE, 10 * MapAttributeSet.CELL_SIZE);

    // when
    var result = mapAttributeSet.hasAttribute(coordinates, MapAttribute.BLOCK);

    // then
    assertThat(result).isTrue();
  }

  @Test
  public void givenMultipleAttributesPresent_whenHasAttribute_thenReturnTrueForEach() {
    // given
    var baseCoordinates = new Coordinates(0, 0);
    var array = new int[MapAttributeSet.CELLS_PER_SECTREE];
    array[0] = MapAttribute.BLOCK.getValue() | MapAttribute.WATER.getValue();
    var sectree = new MapAttributeSectree(array);
    var sectrees = new MapAttributeSectree[1][1];
    sectrees[0][0] = sectree;
    var mapAttributeSet = new MapAttributeSet(1, 1, baseCoordinates, sectrees);

    var coordinates = new Coordinates(0, 0);

    // when & then
    assertThat(mapAttributeSet.hasAttribute(coordinates, MapAttribute.BLOCK)).isTrue();
    assertThat(mapAttributeSet.hasAttribute(coordinates, MapAttribute.WATER)).isTrue();
    assertThat(mapAttributeSet.hasAttribute(coordinates, MapAttribute.WARP)).isFalse();
  }
}
