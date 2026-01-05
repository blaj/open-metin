package com.blaj.openmetin.game.infrastructure.converter;

import com.blaj.openmetin.game.domain.enums.common.ImmuneType;
import jakarta.persistence.Converter;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class ImmuneTypeEnumSetAttributeConverter
    extends EnumSetJsonbAttributeConverter<ImmuneType> {

  public ImmuneTypeEnumSetAttributeConverter(JsonMapper jsonMapper) {
    super(ImmuneType.class, jsonMapper);
  }
}
