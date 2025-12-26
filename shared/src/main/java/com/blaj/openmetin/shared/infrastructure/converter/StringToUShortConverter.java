package com.blaj.openmetin.shared.infrastructure.converter;

import java.util.Optional;
import org.joou.UShort;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

public class StringToUShortConverter implements Converter<String, UShort> {

  @Override
  public @Nullable UShort convert(String source) {
    return Optional.ofNullable(source).map(UShort::valueOf).orElse(null);
  }
}
