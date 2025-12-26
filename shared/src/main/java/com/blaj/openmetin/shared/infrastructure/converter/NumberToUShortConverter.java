package com.blaj.openmetin.shared.infrastructure.converter;

import java.util.Optional;
import org.joou.UShort;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

public class NumberToUShortConverter implements Converter<Number, UShort> {

  @Override
  public @Nullable UShort convert(Number source) {
    return Optional.ofNullable(source).map(Number::intValue).map(UShort::valueOf).orElse(null);
  }
}
