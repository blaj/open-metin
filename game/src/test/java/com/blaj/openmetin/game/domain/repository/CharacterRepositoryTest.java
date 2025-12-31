package com.blaj.openmetin.game.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.domain.entity.Character;
import com.blaj.openmetin.game.domain.enums.character.ClassType;
import com.blaj.openmetin.game.domain.enums.character.Empire;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
public class CharacterRepositoryTest {

  @Autowired private CharacterRepository characterRepository;

  @Test
  public void givenNonExistingEntities_whenFindAllByAccountId_thenReturnEmptyList() {
    // given
    var accountId = 123L;

    // when
    var entities = characterRepository.findAllByAccountId(accountId);

    // then
    assertThat(entities).isEmpty();
  }

  @Test
  public void givenNonExistingAccountId_whenFindAllByAccountId_thenReturnEmptyList() {
    // given
    var entity = characterRepository.save(character(1, 123L));
    var nonExistingAccountId = entity.getAccountId() + 1L;

    // when
    var entities = characterRepository.findAllByAccountId(nonExistingAccountId);

    // then
    assertThat(entities).isEmpty();
  }

  @Test
  public void givenValid_whenFindAllByAccountId_thenReturnEntities() {
    // given
    var accountId = 123L;
    var entity1 = characterRepository.save(character(1, accountId));
    var entity2 = characterRepository.save(character(2, accountId));

    // when
    var entities = characterRepository.findAllByAccountId(accountId);

    // then
    assertThat(entities).isNotEmpty();
    assertThat(entities).contains(entity1, entity2);
  }

  private Character character(int index, long accountId) {
    return Character.builder()
        .name("name#" + index)
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
        .accountId(accountId)
        .build();
  }
}
