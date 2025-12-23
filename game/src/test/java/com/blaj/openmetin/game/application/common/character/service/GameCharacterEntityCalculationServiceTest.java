package com.blaj.openmetin.game.application.common.character.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.domain.entity.Character.ClassType;
import com.blaj.openmetin.game.domain.model.CharacterDto;
import com.blaj.openmetin.game.domain.model.GameCharacterEntity;
import com.blaj.openmetin.game.domain.model.GameSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GameCharacterEntityCalculationServiceTest {

  private GameCharacterEntityCalculationService gameCharacterEntityCalculationService;

  @Mock private GameSession gameSession;

  @BeforeEach
  public void beforeEach() {
    gameCharacterEntityCalculationService = new GameCharacterEntityCalculationService();
  }

  @Test
  public void givenValid_whenCalculate_thenCalculateAllStats() {
    // given
    var classType = ClassType.NINJA_FEMALE;

    var characterDto =
        CharacterDto.builder()
            .id(123L)
            .name("name")
            .classType(classType)
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
            .skillGroup(2)
            .build();

    var gameCharacterEntity =
        GameCharacterEntity.builder()
            .session(gameSession)
            .characterDto(characterDto)
            .empire(characterDto.getEmpire())
            .positionX(characterDto.getPositionX())
            .positionY(characterDto.getPositionY())
            .build();

    // when
    gameCharacterEntityCalculationService.calculate(gameCharacterEntity);

    // then
    var jobConfig = classType.getJobType().getJobConfig();

    assertThat(gameCharacterEntity.getCharacterDto().getMaxHealth())
        .isEqualTo(jobConfig.startHp() + jobConfig.hpPerHt() * 44L + jobConfig.hpPerLevel() * 31L);
    assertThat(gameCharacterEntity.getCharacterDto().getMaxMana())
        .isEqualTo(jobConfig.startSp() + jobConfig.spPerIq() * 63L + jobConfig.spPerLevel() * 31L);
    assertThat(gameCharacterEntity.getDefence()).isEqualTo((long) (31 + Math.floor(0.8 * 44)));
    assertThat(gameCharacterEntity.getMovementSpeed()).isEqualTo((short) 150);
    assertThat(gameCharacterEntity.getAttackSpeed()).isEqualTo((short) 100);
  }
}
