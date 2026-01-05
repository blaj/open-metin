package com.blaj.openmetin.game.infrastructure.converter;

import com.blaj.openmetin.game.domain.enums.monster.MonsterResistType;
import jakarta.persistence.Converter;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class MonsterResistTypeEnumSetAttributeConverter
    extends EnumSetJsonbAttributeConverter<MonsterResistType> {

  public MonsterResistTypeEnumSetAttributeConverter(JsonMapper jsonMapper) {
    super(MonsterResistType.class, jsonMapper);
  }
}
