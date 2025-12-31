package com.blaj.openmetin.game.infrastructure.converter;

import com.blaj.openmetin.game.domain.enums.entity.BattleType;
import jakarta.persistence.Converter;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class BattleTypeEnumSetAttributeConverter
    extends EnumSetJsonbAttributeConverter<BattleType> {

  public BattleTypeEnumSetAttributeConverter(JsonMapper jsonMapper) {
    super(BattleType.class, jsonMapper);
  }
}
