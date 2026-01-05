package com.blaj.openmetin.game.infrastructure.converter;

import com.blaj.openmetin.game.domain.enums.monster.MonsterEnchantType;
import jakarta.persistence.Converter;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class MonsterEnchantTypeEnumSetAttributeConverter
    extends EnumSetJsonbAttributeConverter<MonsterEnchantType> {

  public MonsterEnchantTypeEnumSetAttributeConverter(JsonMapper jsonMapper) {
    super(MonsterEnchantType.class, jsonMapper);
  }
}
