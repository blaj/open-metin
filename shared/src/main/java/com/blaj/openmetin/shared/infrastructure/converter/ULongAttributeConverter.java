package com.blaj.openmetin.shared.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigInteger;
import java.util.Optional;
import org.joou.ULong;
import org.joou.UNumber;

@Converter(autoApply = true)
public class ULongAttributeConverter implements AttributeConverter<ULong, BigInteger> {

  @Override
  public BigInteger convertToDatabaseColumn(ULong uLong) {
    return Optional.ofNullable(uLong).map(UNumber::toBigInteger).orElse(null);
  }

  @Override
  public ULong convertToEntityAttribute(BigInteger bigInteger) {
    return Optional.ofNullable(bigInteger).map(ULong::valueOf).orElse(null);
  }
}
