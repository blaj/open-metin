package com.blaj.openmetin.game.application.common.character.mapper;

import com.blaj.openmetin.game.application.common.character.dto.SimpleCharacterPacket;
import com.blaj.openmetin.game.domain.model.CharacterDto;
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
                    .setLevel(chDto.getLevel())
                    .setPlaytime(chDto.getPlayTime())
                    .setSt(chDto.getSt())
                    .setHt(chDto.getHt())
                    .setDx(chDto.getDx())
                    .setIq(chDto.getIq())
                    .setBodyPart(chDto.getBodyPart())
                    .setHairPart(chDto.getHairPart())
                    .setPositionX(chDto.getPositionX())
                    .setPositionY(chDto.getPositionY()))
        .orElse(null);
  }
}
