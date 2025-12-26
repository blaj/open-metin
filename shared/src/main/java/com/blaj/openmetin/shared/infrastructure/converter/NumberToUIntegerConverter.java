package com.blaj.openmetin.shared.infrastructure.converter;

import java.util.Optional;
import org.joou.UInteger;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

public class NumberToUIntegerConverter implements Converter<Number, UInteger> {

  @Override
  public @Nullable UInteger convert(Number source) {
    return Optional.ofNullable(source).map(Number::longValue).map(UInteger::valueOf).orElse(null);
  }
}
