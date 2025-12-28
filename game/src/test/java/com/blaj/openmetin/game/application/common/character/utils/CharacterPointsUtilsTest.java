package com.blaj.openmetin.game.application.common.character.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.blaj.openmetin.game.domain.enums.character.PointType;
import com.blaj.openmetin.game.domain.model.character.CharacterDto;
import com.blaj.openmetin.game.domain.model.entity.GameCharacterEntity;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CharacterPointsUtilsTest {

  private static Stream<Arguments> providePointTypeTestCases() {
    return Stream.of(
        Arguments.of(PointType.LEVEL, 50L),
        Arguments.of(PointType.EXPERIENCE, 1000000L),
        Arguments.of(PointType.HP, 5000L),
        Arguments.of(PointType.SP, 3000L),
        Arguments.of(PointType.MAX_HP, 10000L),
        Arguments.of(PointType.MAX_SP, 5000L),
        Arguments.of(PointType.ST, 40L),
        Arguments.of(PointType.HT, 30L),
        Arguments.of(PointType.DX, 25L),
        Arguments.of(PointType.IQ, 20L),
        Arguments.of(PointType.ATTACK_SPEED, 100L),
        Arguments.of(PointType.MOVE_SPEED, 150L),
        Arguments.of(PointType.GOLD, 999999L),
        Arguments.of(PointType.MIN_WEAPON_DAMAGE, 100L),
        Arguments.of(PointType.MAX_WEAPON_DAMAGE, 200L),
        Arguments.of(PointType.MIN_ATTACK_DAMAGE, 150L),
        Arguments.of(PointType.MAX_ATTACK_DAMAGE, 250L),
        Arguments.of(PointType.DEFENCE, 80L),
        Arguments.of(PointType.DEFENCE_GRADE, 80L),
        Arguments.of(PointType.STATUS_POINTS, 5L),
        Arguments.of(PointType.PLAY_TIME, 36000L),
        Arguments.of(PointType.SKILL, 3L),
        Arguments.of(PointType.SUB_SKILL, 1L));
  }

  @Test
  public void givenNullGameCharacterEntity_whenGettingPointValue_thenReturnsZero() {
    // given
    var characterEntity = (GameCharacterEntity) null;
    var pointType = PointType.LEVEL;

    // when
    var result = CharacterPointsUtils.getPointValue(characterEntity, pointType);

    // then
    assertThat(result).isZero();
  }

  @Test
  public void givenNullPointType_whenGettingPointValue_thenReturnsZero() {
    // given
    var characterEntity = GameCharacterEntity.builder().build();
    var pointType = (PointType) null;

    // when
    var result = CharacterPointsUtils.getPointValue(characterEntity, pointType);

    // then
    assertThat(result).isZero();
  }

  @Test
  public void givenNullCharacterDto_whenGettingPointValue_thenReturnsZero() {
    // given
    var characterEntity = GameCharacterEntity.builder().characterDto(null).build();
    var pointType = PointType.LEVEL;

    // when
    var result = CharacterPointsUtils.getPointValue(characterEntity, pointType);

    // then
    assertThat(result).isZero();
  }

  @ParameterizedTest(name = "{0} should return {1}")
  @MethodSource("providePointTypeTestCases")
  void givenValidCharacterEntity_whenGettingPointValue_thenReturnsCorrectValue(
      PointType pointType, long expectedValue) {
    // given
    var characterDto =
        CharacterDto.builder()
            .level(50)
            .experience(1000000)
            .health(5000L)
            .mana(3000L)
            .maxHealth(10000L)
            .maxMana(5000L)
            .st(40)
            .ht(30)
            .dx(25)
            .iq(20)
            .gold(999999)
            .minWeaponDamage(100)
            .maxWeaponDamage(200)
            .minAttackDamage(150)
            .maxAttackDamage(250)
            .availableStatusPoints(5)
            .availableSkillPoints(3)
            .playTime(36000L)
            .build();
    var gameCharacterEntity =
        GameCharacterEntity.builder()
            .health(5000L)
            .mana(3000L)
            .attackSpeed((short) 100)
            .movementSpeed((short) 150)
            .defence(80L)
            .characterDto(characterDto)
            .build();

    // when
    var result = CharacterPointsUtils.getPointValue(gameCharacterEntity, pointType);

    // then
    assertThat(result).isEqualTo(expectedValue);
  }
}
