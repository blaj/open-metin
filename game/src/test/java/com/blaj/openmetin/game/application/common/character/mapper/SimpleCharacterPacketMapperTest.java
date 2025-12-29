package com.blaj.openmetin.game.application.common.character.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import org.junit.jupiter.api.Test;

public class SimpleCharacterPacketMapperTest {

  @Test
  public void givenNull_whenMap_thenReturnNull() {
    // given
    var characterDto = (CharacterDto) null;

    // when
    var packet = SimpleCharacterPacketMapper.map(characterDto);

    // then
    assertThat(packet).isNull();
  }

  @Test
  public void givenValid_whenMap_thenReturnPacket() {
    // given
    var characterDto =
        CharacterDto.builder()
            .id(123L)
            .name("name")
            .classType(ClassType.NINJA_FEMALE)
            .level(31)
            .playTime(32432L)
            .st(33)
            .ht(44)
            .dx(76)
            .iq(63)
            .bodyPart(47)
            .hairPart(102)
            .positionX(32423)
            .positionY(43654)
            .build();

    // when
    var packet = SimpleCharacterPacketMapper.map(characterDto);

    // then
    assertThat(packet).isNotNull();
    assertThat(packet.getId()).isEqualTo(characterDto.getId());
    assertThat(packet.getName()).isEqualTo(characterDto.getName());
    assertThat(packet.getClassType()).isEqualTo(characterDto.getClassType());
    assertThat(packet.getLevel()).isEqualTo(characterDto.getLevel().shortValue());
    assertThat(packet.getPlaytime()).isEqualTo(characterDto.getPlayTime());
    assertThat(packet.getSt()).isEqualTo(characterDto.getSt().shortValue());
    assertThat(packet.getHt()).isEqualTo(characterDto.getHt().shortValue());
    assertThat(packet.getDx()).isEqualTo(characterDto.getDx().shortValue());
    assertThat(packet.getIq()).isEqualTo(characterDto.getIq().shortValue());
    assertThat(packet.getBodyPart()).isEqualTo(characterDto.getBodyPart());
    assertThat(packet.getHairPart()).isEqualTo(characterDto.getHairPart());
    assertThat(packet.getPositionX()).isEqualTo(characterDto.getPositionX());
    assertThat(packet.getPositionY()).isEqualTo(characterDto.getPositionY());
  }
}
