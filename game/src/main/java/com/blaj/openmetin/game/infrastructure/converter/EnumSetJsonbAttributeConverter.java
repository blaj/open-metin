package com.blaj.openmetin.game.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
public abstract class EnumSetJsonbAttributeConverter<E extends Enum<E>>
    implements AttributeConverter<EnumSet<E>, String> {

  private final Class<E> enumClass;
  private final JsonMapper jsonMapper;

  @Override
  public String convertToDatabaseColumn(EnumSet<E> attribute) {
    return Optional.ofNullable(attribute)
        .filter(a -> !a.isEmpty())
        .map(jsonMapper::writeValueAsString)
        .orElse(null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public EnumSet<E> convertToEntityAttribute(String data) {
    return Optional.ofNullable(data)
        .filter(d -> !d.isBlank())
        .map(
            d ->
                (List<E>)
                    jsonMapper.readValue(
                        d,
                        jsonMapper
                            .getTypeFactory()
                            .constructCollectionLikeType(List.class, enumClass)))
        .filter(d -> !d.isEmpty())
        .map(EnumSet::copyOf)
        .orElse(EnumSet.noneOf(enumClass));
  }
}
