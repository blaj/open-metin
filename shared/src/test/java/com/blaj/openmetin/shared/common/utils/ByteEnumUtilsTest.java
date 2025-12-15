package com.blaj.openmetin.shared.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import org.junit.jupiter.api.Test;

public class ByteEnumUtilsTest {

  @Test
  public void givenValidValue_whenFromValue_thenReturnsEnum() {
    // given

    // when
    var result = ByteEnumUtils.fromValue(TestByteEnum.class, (byte) 2);

    // then
    assertThat(result).isEqualTo(TestByteEnum.SECOND);
  }

  @Test
  public void givenInvalidValue_whenFromValue_thenThrowsException() {
    // given

    // when
    var thrownException =
        assertThrows(
            IllegalArgumentException.class,
            () -> ByteEnumUtils.fromValue(TestByteEnum.class, (byte) 99));

    // then
    assertThat(thrownException).hasMessageContaining("Unknown TestByteEnum value: 99");
  }

  @Test
  public void givenNullEnumClass_whenFromValue_thenThrowsException() {
    // given

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> ByteEnumUtils.fromValue(null, (byte) 1));

    // then
    assertThat(thrownException).hasMessageContaining("Unknown null value: 1");
  }

  @Test
  public void givenFirstValue_whenFromValue_thenReturnsFirstEnum() {
    // given

    // when
    var result = ByteEnumUtils.fromValue(TestByteEnum.class, (byte) 1);

    // then
    assertThat(result).isEqualTo(TestByteEnum.FIRST);
  }

  @Test
  public void givenLastValue_whenFromValue_thenReturnsLastEnum() {
    // given

    // when
    var result = ByteEnumUtils.fromValue(TestByteEnum.class, (byte) 3);

    // then
    assertThat(result).isEqualTo(TestByteEnum.THIRD);
  }

  enum TestByteEnum implements ByteEnum {
    FIRST((byte) 1),
    SECOND((byte) 2),
    THIRD((byte) 3);

    private final byte value;

    TestByteEnum(byte value) {
      this.value = value;
    }

    @Override
    public byte getValue() {
      return value;
    }
  }
}
