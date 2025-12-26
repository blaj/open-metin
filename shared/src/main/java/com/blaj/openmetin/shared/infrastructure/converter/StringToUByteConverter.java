package com.blaj.openmetin.shared.infrastructure.converter;

import java.util.Optional;
import org.joou.UByte;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

public class StringToUByteConverter implements Converter<String, UByte> {

  @Override
  public @Nullable UByte convert(String source) {
    return Optional.ofNullable(source).map(UByte::valueOf).orElse(null);
  }
}
