package com.blaj.openmetin.authentication.infrastructure.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

public class RequestEnumConverterTest {
  private Converter<String, ? extends Enum<?>> converter;

  @BeforeEach
  public void beforeEach() {
    var requestEnumConverter = new RequestEnumConverter();

    this.converter = requestEnumConverter.getConverter(TestEnum.class);
  }

  @Test
  public void givenNotValid_whenConvert_thenThrowException() {
    // given
    var nonExistingEnum = "nonExisting";

    // when
    var thrownException =
        assertThrows(IllegalArgumentException.class, () -> this.converter.convert(nonExistingEnum));

    // then
    assertThat(thrownException.getMessage()).contains("No enum constant");
  }

  @Test
  public void givenValid_whenConvert_thenReturnEnum() {
    // given
    var existingEnum = "test";

    // when
    var converted = this.converter.convert(existingEnum);

    // then
    assertThat(converted).isEqualTo(TestEnum.TEST);
  }

  private enum TestEnum {
    TEST
  }
}