package com.blaj.openmetin.game.infrastructure.converter;

import com.blaj.openmetin.game.domain.enums.monster.MonsterRaceType;
import jakarta.persistence.Converter;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class MonsterRaceTypeEnumSetAttributeConverter
    extends EnumSetJsonbAttributeConverter<MonsterRaceType> {

  public MonsterRaceTypeEnumSetAttributeConverter(JsonMapper jsonMapper) {
    super(MonsterRaceType.class, jsonMapper);
  }
}
