package com.blaj.openmetin.shared.infrastructure.converter;

import java.util.Optional;
import org.joou.UByte;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

public class NumberToUByteConverter implements Converter<Number, UByte> {

  @Override
  public @Nullable UByte convert(Number source) {
    return Optional.ofNullable(source).map(Number::shortValue).map(UByte::valueOf).orElse(null);
  }
}
