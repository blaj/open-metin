package com.blaj.openmetin.shared.infrastructure.converter;

import java.util.Optional;
import org.joou.ULong;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

public class StringToULongConverter implements Converter<String, ULong> {

  @Override
  public @Nullable ULong convert(String source) {
    return Optional.ofNullable(source).map(ULong::valueOf).orElse(null);
  }
}
