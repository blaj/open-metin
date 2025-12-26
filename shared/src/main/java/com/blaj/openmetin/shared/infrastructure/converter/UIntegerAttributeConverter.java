package com.blaj.openmetin.shared.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Optional;
import org.joou.UInteger;

@Converter(autoApply = true)
public class UIntegerAttributeConverter implements AttributeConverter<UInteger, Long> {

  @Override
  public Long convertToDatabaseColumn(UInteger uInteger) {
    return Optional.ofNullable(uInteger).map(UInteger::longValue).orElse(null);
  }

  @Override
  public UInteger convertToEntityAttribute(Long aLong) {
    return Optional.ofNullable(aLong).map(UInteger::valueOf).orElse(null);
  }
}
