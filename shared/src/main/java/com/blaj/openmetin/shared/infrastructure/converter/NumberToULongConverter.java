package com.blaj.openmetin.shared.infrastructure.converter;

import java.util.Optional;
import org.joou.ULong;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

public class NumberToULongConverter implements Converter<Number, ULong> {

  @Override
  public @Nullable ULong convert(Number source) {
    return Optional.ofNullable(source).map(Number::longValue).map(ULong::valueOf).orElse(null);
  }
}
