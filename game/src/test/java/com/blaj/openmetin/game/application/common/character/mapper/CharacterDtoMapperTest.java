package com.blaj.openmetin.game.application.common.character.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.domain.entity.Character;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import org.junit.jupiter.api.Test;

public class CharacterDtoMapperTest {

  @Test
  public void givenNull_whenMap_thenReturnNull() {
    // given
    var character = (Character) null;

    // when
    var dto = CharacterDtoMapper.map(character);

    // then
    assertThat(dto).isNull();
  }

  @Test
  public void givenValid_whenMap_thenReturnDto() {
    // given
    var character =
        Character.builder()
            .id(123L)
            .name("name")
            .slot(3)
            .bodyPart(10)
            .hairPart(20)
            .empire(Empire.CHUNJO)
            .classType(ClassType.SHAMAN_FEMALE)
            .level(33)
            .experience(4312)
            .health(444L)
            .mana(555L)
            .stamina(666L)
            .st(20)
            .ht(30)
            .dx(40)
            .iq(50)
            .givenStatusPoints(60)
            .availableStatusPoints(70)
            .availableSkillPoints(80)
            .gold(100000)
            .positionX(123432)
            .positionY(43322)
            .playTime(324543L)
            .accountId(325234L)
            .build();

    // when
    var dto = CharacterDtoMapper.map(character);

    // then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(character.getId());
    assertThat(dto.getName()).isEqualTo(character.getName());
    assertThat(dto.getSlot()).isEqualTo(character.getSlot());
    assertThat(dto.getBodyPart()).isEqualTo(character.getBodyPart());
    assertThat(dto.getHairPart()).isEqualTo(character.getHairPart());
    assertThat(dto.getEmpire()).isEqualTo(character.getEmpire());
    assertThat(dto.getClassType()).isEqualTo(character.getClassType());
    assertThat(dto.getLevel()).isEqualTo(character.getLevel());
    assertThat(dto.getExperience()).isEqualTo(character.getExperience());
    assertThat(dto.getHealth()).isEqualTo(character.getHealth());
    assertThat(dto.getMana()).isEqualTo(character.getMana());
    assertThat(dto.getStamina()).isEqualTo(character.getStamina());
    assertThat(dto.getSt()).isEqualTo(character.getSt());
    assertThat(dto.getHt()).isEqualTo(character.getHt());
    assertThat(dto.getDx()).isEqualTo(character.getDx());
    assertThat(dto.getIq()).isEqualTo(character.getIq());
    assertThat(dto.getGivenStatusPoints()).isEqualTo(character.getGivenStatusPoints());
    assertThat(dto.getAvailableStatusPoints()).isEqualTo(character.getAvailableStatusPoints());
    assertThat(dto.getAvailableSkillPoints()).isEqualTo(character.getAvailableSkillPoints());
    assertThat(dto.getGold()).isEqualTo(character.getGold());
    assertThat(dto.getPositionX()).isEqualTo(character.getPositionX());
    assertThat(dto.getPositionY()).isEqualTo(character.getPositionY());
    assertThat(dto.getPlayTime()).isEqualTo(character.getPlayTime());
    assertThat(dto.getAccountId()).isEqualTo(character.getAccountId());
  }
}
