package com.blaj.openmetin.game.application.common.character.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.blaj.openmetin.game.application.common.character.mapper.CharacterDtoMapper;
import com.blaj.openmetin.game.domain.entity.Character;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.domain.repository.CharacterRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CharacterServiceTest {

  private CharacterService characterService;

  @Mock private CharacterRepository characterRepository;

  @BeforeEach
  public void beforeEach() {
    characterService = new CharacterService(characterRepository);
  }

  @Test
  public void givenNonExistingCharacter_whenGetCharacter_thenReturnEmptyList() {
    // given
    var accountId = 123L;

    given(characterRepository.findAllByAccountId(accountId)).willReturn(Collections.emptyList());

    // when
    var characterList = characterService.getCharacters(accountId);

    // then
    assertThat(characterList).isEmpty();
  }

  @Test
  public void givenValid_whenGetCharacter_thenReturnDtoList() {
    // given
    var accountId = 123L;
    var character1 = character(1L);
    var character2 = character(2L);

    given(characterRepository.findAllByAccountId(accountId))
        .willReturn(List.of(character1, character2));

    // when
    var characterList = characterService.getCharacters(accountId);

    // then
    assertThat(characterList).isNotEmpty();
    assertThat(characterList).hasSize(2);
    assertThat(characterList)
        .contains(CharacterDtoMapper.map(character1), CharacterDtoMapper.map(character2));
  }

  private Character character(long id) {
    return Character.builder()
        .id(id)
        .name("name#" + id)
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
  }
}
