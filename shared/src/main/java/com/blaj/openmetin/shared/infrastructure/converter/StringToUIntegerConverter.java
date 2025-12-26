package com.blaj.openmetin.shared.infrastructure.converter;

import java.util.Optional;
import org.joou.UInteger;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

public class StringToUIntegerConverter implements Converter<String, UInteger> {

  @Override
  public @Nullable UInteger convert(String source) {
    return Optional.ofNullable(source).map(UInteger::valueOf).orElse(null);
  }
}
