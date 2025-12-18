package com.blaj.openmetin.authentication.infrastructure.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

public class RequestEnumConverter implements ConverterFactory<String, Enum> {

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
    return source -> (T) Enum.valueOf(targetType, source.trim().toUpperCase());
  }
}
