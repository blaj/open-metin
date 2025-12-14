package com.blaj.openmetin.game.application.common.character.mapper;

import com.blaj.openmetin.game.application.common.character.dto.CharacterDto;
import com.blaj.openmetin.game.application.common.character.dto.CharacterListPacket.SimpleCharacterPacket;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SimpleCharacterPacketMapper {

  public static SimpleCharacterPacket map(CharacterDto characterDto) {
    return Optional.ofNullable(characterDto)
        .map(
            chDto ->
                new SimpleCharacterPacket()
                    .setId(chDto.getId())
                    .setName(chDto.getName())
                    .setClassType(chDto.getClassType())
                    .setLevel(chDto.getLevel().shortValue())
                    .setPlaytime(chDto.getPlayTime())
                    .setSt(chDto.getSt().shortValue())
                    .setHt(chDto.getHt().shortValue())
                    .setDx(chDto.getDx().shortValue())
                    .setIq(chDto.getIq().shortValue())
                    .setBodyPart(chDto.getBodyPart())
                    .setHairPart(chDto.getHairPart())
                    .setPositionX(chDto.getPositionX())
                    .setPositionY(chDto.getPositionY()))
        .orElse(null);
  }
}
