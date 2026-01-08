package com.blaj.openmetin.game.domain.model.map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.blaj.openmetin.game.domain.enums.map.MapAttribute;
import com.blaj.openmetin.shared.domain.model.Coordinates;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MapTest {

  @Mock private MapAttributeSet mapAttributeSet;

  @ParameterizedTest
  @CsvSource({
    "102400, 819200, true",
    "153599, 870399, true",
    "128000, 844800, true",
    "102399, 819200, false",
    "153600, 819200, false",
    "102400, 818199, false",
    "102400, 870400, false"
  })
  void givenCoordinates_whenIsPositionInside_thenReturnsCorrectResult(
      int x, int y, boolean expectedInside) {
    // given
    var map =
        new Map(
            "test_map",
            new Coordinates(102400, 819200),
            2,
            2,
            TownCoordinates.allOf(new Coordinates(1000, 2000)));
    var coordinates = new Coordinates(x, y);

    // when
    var result = map.isPositionInside(coordinates);

    // then
    assertThat(result).isEqualTo(expectedInside);
  }

  @Test
  void givenMapWithoutAttributeSet_whenHasAnyMapAttributeOnCoordinates_thenReturnsFalse() {
    // given
    var map = new Map("test_map", new Coordinates(102400, 819200), 2, 2, null);
    var coordinates = new Coordinates(128000, 844800);
    var attributes = EnumSet.of(MapAttribute.BLOCK);

    // when
    var result = map.hasAnyMapAttributeOnCoordinates(coordinates, attributes);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void givenMapWithAttributeSet_whenHasAnyMapAttributeOnCoordinates_thenDelegatesToAttributeSet() {
    // given
    var map = new Map("test_map", new Coordinates(102400, 819200), 2, 2, null);
    map.setMapAttributeSet(mapAttributeSet);

    var coordinates = new Coordinates(128000, 844800);
    var attributes = EnumSet.of(MapAttribute.BLOCK);

    given(mapAttributeSet.hasAnyAttribute(coordinates, attributes)).willReturn(true);

    // when
    var result = map.hasAnyMapAttributeOnCoordinates(coordinates, attributes);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void givenSameStartAndEnd_whenHasAttributeOnStraightPath_thenChecksOnlyStartPosition() {
    // given
    var map = new Map("test_map", new Coordinates(102400, 819200), 2, 2, null);
    map.setMapAttributeSet(mapAttributeSet);

    var coordinates = new Coordinates(128000, 844800);
    var attributes = EnumSet.of(MapAttribute.BLOCK);

    given(mapAttributeSet.hasAnyAttribute(coordinates, attributes)).willReturn(true);

    // when
    var result = map.hasAttributeOnStraightPath(coordinates, coordinates, attributes);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void givenPathWithBlockAttribute_whenHasAttributeOnStraightPath_thenReturnsTrue() {
    // given

    var map = new Map("test_map", new Coordinates(102400, 819200), 2, 2, null);
    map.setMapAttributeSet(mapAttributeSet);

    var start = new Coordinates(102400, 819200);
    var end = new Coordinates(110000, 826800);
    var attributes = EnumSet.of(MapAttribute.BLOCK);

    given(mapAttributeSet.hasAnyAttribute(any(Coordinates.class), eq(attributes)))
        .willReturn(false)
        .willReturn(true);

    // when
    var result = map.hasAttributeOnStraightPath(start, end, attributes);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void givenPathWithoutAttributes_whenHasAttributeOnStraightPath_thenReturnsFalse() {
    // given
    var map = new Map("test_map", new Coordinates(102400, 819200), 2, 2, null);
    map.setMapAttributeSet(mapAttributeSet);

    var start = new Coordinates(102400, 819200);
    var end = new Coordinates(110000, 826800);
    var attributes = EnumSet.of(MapAttribute.BLOCK);

    given(mapAttributeSet.hasAnyAttribute(any(Coordinates.class), eq(attributes)))
        .willReturn(false);

    // when
    var result = map.hasAttributeOnStraightPath(start, end, attributes);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void givenMapCoordinates_whenGetUnitX_thenReturnsCorrectUnit() {
    // when
    var map = new Map("test_map", new Coordinates(102400, 819200), 2, 2, null);
    var unitX = map.getUnitX();

    // then
    assertThat(unitX).isEqualTo(4); // 102400 / 25600 = 4
  }

  @Test
  void givenMapCoordinates_whenGetUnitY_thenReturnsCorrectUnit() {
    // when
    var map = new Map("test_map", new Coordinates(102400, 819200), 2, 2, null);
    var unitY = map.getUnitY();

    // then
    assertThat(unitY).isEqualTo(32); // 819200 / 25600 = 32
  }
}
