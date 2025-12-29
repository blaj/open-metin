package com.blaj.openmetin.game.domain.model.map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.blaj.openmetin.game.domain.enums.map.MapAttribute;
import org.junit.jupiter.api.Test;

public class MapAttributeSectreeTest {

  @Test
  public void givenInvalidArraySize_whenCreateMapAttributeSectree_thenThrowException() {
    // given
    var invalidArray = new int[100];

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> new MapAttributeSectree(invalidArray));

    // then
    assertThat(thrownException).hasMessageContaining("Invalid sectree size");
  }

  @Test
  public void givenValidArraySize_whenCreateMapAttributeSectree_thenCreateObject() {
    // given
    var validArray = new int[MapAttributeSectree.CELLS_PER_SECTREE];

    // when
    var sectree = new MapAttributeSectree(validArray);

    // then
    assertThat(sectree).isNotNull();
    assertThat(sectree.cellAttributes()).hasSize(MapAttributeSectree.CELLS_PER_SECTREE);
  }

  @Test
  public void givenEmptyConstant_whenAccess_thenReturnValidSectree() {
    // when
    var emptySectree = MapAttributeSectree.EMPTY;

    // then
    assertThat(emptySectree).isNotNull();
    assertThat(emptySectree.cellAttributes()).hasSize(MapAttributeSectree.CELLS_PER_SECTREE);
  }

  @Test
  public void givenNegativeX_whenGet_thenReturnNone() {
    // given
    var sectree = new MapAttributeSectree(new int[MapAttributeSectree.CELLS_PER_SECTREE]);

    // when
    var result = sectree.get(-1, 10);

    // then
    assertThat(result).isEqualTo(MapAttribute.NONE.getValue());
  }

  @Test
  public void givenNegativeY_whenGet_thenReturnNone() {
    // given
    var sectree = new MapAttributeSectree(new int[MapAttributeSectree.CELLS_PER_SECTREE]);

    // when
    var result = sectree.get(10, -1);

    // then
    assertThat(result).isEqualTo(MapAttribute.NONE.getValue());
  }

  @Test
  public void givenXOutOfBounds_whenGet_thenReturnNone() {
    // given
    var sectree = new MapAttributeSectree(new int[MapAttributeSectree.CELLS_PER_SECTREE]);

    // when
    var result = sectree.get(128, 10);

    // then
    assertThat(result).isEqualTo(MapAttribute.NONE.getValue());
  }

  @Test
  public void givenYOutOfBounds_whenGet_thenReturnNone() {
    // given
    var sectree = new MapAttributeSectree(new int[MapAttributeSectree.CELLS_PER_SECTREE]);

    // when
    var result = sectree.get(10, 128);

    // then
    assertThat(result).isEqualTo(MapAttribute.NONE.getValue());
  }

  @Test
  public void givenValidCoordinates_whenGet_thenReturnValue() {
    // given
    var array = new int[MapAttributeSectree.CELLS_PER_SECTREE];
    array[10 * MapAttributeSectree.CELLS_PER_AXIS + 5] = 42;
    var sectree = new MapAttributeSectree(array);

    // when
    var result = sectree.get(5, 10);

    // then
    assertThat(result).isEqualTo(42);
  }

  @Test
  public void givenCoordinates_whenGet_thenCalculateIndexCorrectly() {
    // given
    var array = new int[MapAttributeSectree.CELLS_PER_SECTREE];
    array[0] = 1; // (0, 0)
    array[127] = 2; // (127, 0)
    array[128] = 3; // (0, 1)
    array[MapAttributeSectree.CELLS_PER_SECTREE - 1] = 4; // (127, 127)
    var sectree = new MapAttributeSectree(array);

    // when & then
    assertThat(sectree.get(0, 0)).isEqualTo(1);
    assertThat(sectree.get(127, 0)).isEqualTo(2);
    assertThat(sectree.get(0, 1)).isEqualTo(3);
    assertThat(sectree.get(127, 127)).isEqualTo(4);
  }

  @Test
  public void givenCoordinatesOutOfBounds_whenHasAttribute_thenReturnFalse() {
    // given
    var sectree = new MapAttributeSectree(new int[MapAttributeSectree.CELLS_PER_SECTREE]);

    // when
    var result = sectree.hasAttribute(-1, 10, MapAttribute.BLOCK);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenAttributeNotPresent_whenHasAttribute_thenReturnFalse() {
    // given
    var array = new int[MapAttributeSectree.CELLS_PER_SECTREE];
    array[10 * MapAttributeSectree.CELLS_PER_AXIS + 5] = MapAttribute.WATER.getValue();
    var sectree = new MapAttributeSectree(array);

    // when
    var result = sectree.hasAttribute(5, 10, MapAttribute.BLOCK);

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void givenAttributePresent_whenHasAttribute_thenReturnTrue() {
    // given
    var array = new int[MapAttributeSectree.CELLS_PER_SECTREE];
    array[10 * MapAttributeSectree.CELLS_PER_AXIS + 5] = MapAttribute.BLOCK.getValue();
    var sectree = new MapAttributeSectree(array);

    // when
    var result = sectree.hasAttribute(5, 10, MapAttribute.BLOCK);

    // then
    assertThat(result).isTrue();
  }

  @Test
  public void givenMultipleAttributesPresent_whenHasAttribute_thenReturnTrueForEach() {
    // given
    var array = new int[MapAttributeSectree.CELLS_PER_SECTREE];
    array[10 * MapAttributeSectree.CELLS_PER_AXIS + 5] =
        MapAttribute.BLOCK.getValue() | MapAttribute.WATER.getValue();
    var sectree = new MapAttributeSectree(array);

    // when & then
    assertThat(sectree.hasAttribute(5, 10, MapAttribute.BLOCK)).isTrue();
    assertThat(sectree.hasAttribute(5, 10, MapAttribute.WATER)).isTrue();
    assertThat(sectree.hasAttribute(5, 10, MapAttribute.WARP)).isFalse();
  }
}
