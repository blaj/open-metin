package com.blaj.openmetin.shared.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Optional;
import org.joou.UShort;

@Converter(autoApply = true)
public class UShortAttributeConverter implements AttributeConverter<UShort, Integer> {

  @Override
  public Integer convertToDatabaseColumn(UShort uShort) {
    return Optional.ofNullable(uShort).map(Number::intValue).orElse(null);
  }

  @Override
  public UShort convertToEntityAttribute(Integer integer) {
    return Optional.ofNullable(integer).map(UShort::valueOf).orElse(null);
  }
}
