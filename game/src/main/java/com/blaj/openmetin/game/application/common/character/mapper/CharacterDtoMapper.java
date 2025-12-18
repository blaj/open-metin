package com.blaj.openmetin.game.application.common.character.mapper;

import com.blaj.openmetin.game.domain.entity.Character;
import com.blaj.openmetin.game.domain.model.CharacterDto;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CharacterDtoMapper {

  public static CharacterDto map(Character character) {
    return Optional.ofNullable(character)
        .map(
            ch ->
                CharacterDto.builder()
                    .id(ch.getId())
                    .name(ch.getName())
                    .slot(ch.getSlot())
                    .bodyPart(ch.getBodyPart())
                    .hairPart(ch.getHairPart())
                    .empire(ch.getEmpire())
                    .classType(ch.getClassType())
                    .level(ch.getLevel())
                    .experience(ch.getExperience())
                    .health(ch.getHealth())
                    .mana(ch.getMana())
                    .stamina(ch.getStamina())
                    .st(ch.getSt())
                    .ht(ch.getHt())
                    .dx(ch.getDx())
                    .iq(ch.getIq())
                    .givenStatusPoints(ch.getGivenStatusPoints())
                    .availableStatusPoints(ch.getAvailableStatusPoints())
                    .availableSkillPoints(ch.getAvailableSkillPoints())
                    .gold(ch.getGold())
                    .positionX(ch.getPositionX())
                    .positionY(ch.getPositionY())
                    .playTime(ch.getPlayTime())
                    .accountId(ch.getAccountId())
                    .build())
        .orElse(null);
  }
}
