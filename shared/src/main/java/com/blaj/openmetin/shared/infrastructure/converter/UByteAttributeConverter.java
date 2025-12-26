package com.blaj.openmetin.shared.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Optional;
import org.joou.UByte;

@Converter(autoApply = true)
public class UByteAttributeConverter implements AttributeConverter<UByte, Short> {

  @Override
  public Short convertToDatabaseColumn(UByte uByte) {
    return Optional.ofNullable(uByte).map(Number::shortValue).orElse(null);
  }

  @Override
  public UByte convertToEntityAttribute(Short aShort) {
    return Optional.ofNullable(aShort).map(UByte::valueOf).orElse(null);
  }
}
