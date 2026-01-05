package com.blaj.openmetin.game.infrastructure.converter;

import com.blaj.openmetin.game.domain.enums.entity.AiFlag;
import jakarta.persistence.Converter;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class AiFlagEnumSetAttributeConverter extends EnumSetJsonbAttributeConverter<AiFlag> {

  public AiFlagEnumSetAttributeConverter(JsonMapper jsonMapper) {
    super(AiFlag.class, jsonMapper);
  }
}
