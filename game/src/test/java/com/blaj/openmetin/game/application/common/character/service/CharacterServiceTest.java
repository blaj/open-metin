package com.blaj.openmetin.game.application.common.character.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.blaj.openmetin.game.application.common.character.mapper.CharacterDtoMapper;
import com.blaj.openmetin.game.application.common.empire.SelectEmpireService;
import com.blaj.openmetin.game.domain.config.EmpireSpawnConfigs;
import com.blaj.openmetin.game.domain.entity.Character;
import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.entity.Character.Empire;
import com.blaj.openmetin.game.domain.repository.CharacterRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CharacterServiceTest {

  private CharacterService characterService;

  @Mock private CharacterRepository characterRepository;
  @Mock private SelectEmpireService selectEmpireService;

  @BeforeEach
  public void beforeEach() {
    characterService = new CharacterService(characterRepository, selectEmpireService);
  }

  @Test
  public void givenNonExistingCharacter_whenGetCharacters_thenReturnEmptyList() {
    // given
    var accountId = 123L;

    given(characterRepository.findAllByAccountId(accountId)).willReturn(Collections.emptyList());

    // when
    var characterList = characterService.getCharacters(accountId);

    // then
    assertThat(characterList).isEmpty();
  }

  @Test
  public void givenValid_whenGetCharacters_thenReturnDtoList() {
    // given
    var accountId = 123L;
    var character1 = character(1L, 1);
    var character2 = character(2L, 2);

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

  @Test
  public void givenNonExistingCharacter_whenGetCharacter_thenReturnEmptyOptional() {
    // given
    var accountId = 123L;
    var slot = 1;

    given(characterRepository.findAllByAccountId(accountId))
        .willReturn(List.of(character(111L, slot + 1)));

    // when
    var characterDto = characterService.getCharacter(accountId, (short) slot);

    // then
    assertThat(characterDto).isEmpty();
  }

  @Test
  public void givenValid_whenGetCharacter_thenReturnDtoOptional() {
    // given
    var accountId = 123L;
    var slot = 1;
    var character = character(111L, slot);

    given(characterRepository.findAllByAccountId(accountId)).willReturn(List.of(character));

    // when
    var characterDto = characterService.getCharacter(accountId, (short) slot);

    // then
    assertThat(characterDto).isNotEmpty();
    assertThat(characterDto).contains(CharacterDtoMapper.map(character));
  }

  @Test
  public void givenNonExistingCharacter_whenCreate_thenGetEmpireFromCache() {
    // given
    var accountId = 123L;
    var name = "name";
    var classType = ClassType.SHAMAN_FEMALE;
    var shape = 2;
    var slot = 3;
    var empire = Empire.JINNO;
    var empireSpawnConfig = EmpireSpawnConfigs.getSpawnPosition(empire);
    var jobConfig = classType.getJobType().getJobConfig();
    var character =
        Character.builder()
            .name(name)
            .classType(classType)
            .empire(empire)
            .positionX(empireSpawnConfig.x())
            .positionY(empireSpawnConfig.y())
            .st(jobConfig.st())
            .ht(jobConfig.ht())
            .dx(jobConfig.dx())
            .iq(jobConfig.iq())
            .health((long) jobConfig.startHp())
            .mana((long) jobConfig.startSp())
            .slot(slot)
            .basePart(shape)
            .accountId(accountId)
            .build();

    given(characterRepository.findAllByAccountId(accountId)).willReturn(List.of(character));
    given(characterRepository.save(character)).willReturn(character);

    // when
    var createdCharacter =
        characterService.create(accountId, name, classType, (short) shape, (short) slot);

    // then
    then(characterRepository).should().save(character);

    assertThat(createdCharacter).isEqualTo(CharacterDtoMapper.map(character));
  }

  @Test
  public void givenExistingCharacter_whenCreate_thenSaveAndReturnDto() {
    // given
    var accountId = 123L;
    var name = "name";
    var classType = ClassType.SHAMAN_FEMALE;
    var shape = 2;
    var slot = 3;
    var empire = Empire.JINNO;
    var empireSpawnConfig = EmpireSpawnConfigs.getSpawnPosition(empire);
    var jobConfig = classType.getJobType().getJobConfig();
    var character =
        Character.builder()
            .name(name)
            .classType(classType)
            .empire(empire)
            .positionX(empireSpawnConfig.x())
            .positionY(empireSpawnConfig.y())
            .st(jobConfig.st())
            .ht(jobConfig.ht())
            .dx(jobConfig.dx())
            .iq(jobConfig.iq())
            .health((long) jobConfig.startHp())
            .mana((long) jobConfig.startSp())
            .slot(slot)
            .basePart(shape)
            .accountId(accountId)
            .build();

    given(characterRepository.findAllByAccountId(accountId)).willReturn(Collections.emptyList());
    given(selectEmpireService.getFromCache(accountId)).willReturn(empire);
    given(characterRepository.save(character)).willReturn(character);

    // when
    var createdCharacter =
        characterService.create(accountId, name, classType, (short) shape, (short) slot);

    // then
    then(characterRepository).should().save(character);

    assertThat(createdCharacter).isEqualTo(CharacterDtoMapper.map(character));
  }

  @Test
  public void givenNonExistingCharacter_whenDelete_thenDoNothing() {
    // given
    var accountId = 123L;
    var slot = 2;

    given(characterRepository.findByAccountIdAndSlot(accountId, slot)).willReturn(Optional.empty());

    // when
    characterService.delete(accountId, (short) slot);

    // then
    then(characterRepository).should(never()).deleteById(anyLong());
  }

  @Test
  public void givenValid_whenDelete_thenDeleteById() {
    // given
    var accountId = 123L;
    var slot = 2;
    var characterId = 333L;
    var character = Character.builder().id(characterId).build();

    given(characterRepository.findByAccountIdAndSlot(accountId, slot))
        .willReturn(Optional.of(character));

    // when
    characterService.delete(accountId, (short) slot);

    // then
    then(characterRepository).should().deleteById(characterId);
  }

  private Character character(long id, int slot) {
    return Character.builder()
        .id(id)
        .name("name#" + id)
        .slot(slot)
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
