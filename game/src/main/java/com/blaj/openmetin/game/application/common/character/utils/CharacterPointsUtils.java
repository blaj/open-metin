package com.blaj.openmetin.game.application.common.character.utils;

import com.blaj.openmetin.game.domain.enums.PointType;
import com.blaj.openmetin.game.domain.model.GameCharacterEntity;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CharacterPointsUtils {

  public static long getPointValue(GameCharacterEntity gameCharacterEntity, PointType pointType) {
    if (gameCharacterEntity == null) {
      return 0L;
    }

    if (pointType == null) {
      return 0L;
    }

    var characterDto = gameCharacterEntity.getCharacterDto();

    if (characterDto == null) {
      return 0L;
    }

    return switch (pointType) {
      case LEVEL -> characterDto.getLevel();
      case EXPERIENCE -> characterDto.getExperience();
      case HP -> gameCharacterEntity.getHealth();
      case SP -> gameCharacterEntity.getMana();
      case MAX_HP -> characterDto.getMaxHealth();
      case MAX_SP -> characterDto.getMaxMana();
      case ST -> characterDto.getSt();
      case HT -> characterDto.getHt();
      case DX -> characterDto.getDx();
      case IQ -> characterDto.getIq();
      case ATTACK_SPEED -> gameCharacterEntity.getAttackSpeed();
      case MOVE_SPEED -> gameCharacterEntity.getMovementSpeed();
      case GOLD -> characterDto.getGold();
      case MIN_WEAPON_DAMAGE -> characterDto.getMinWeaponDamage();
      case MAX_WEAPON_DAMAGE -> characterDto.getMaxWeaponDamage();
      case MIN_ATTACK_DAMAGE -> characterDto.getMinAttackDamage();
      case MAX_ATTACK_DAMAGE -> characterDto.getMaxAttackDamage();
      case DEFENCE, DEFENCE_GRADE -> gameCharacterEntity.getDefence();
      case STATUS_POINTS -> characterDto.getAvailableStatusPoints();
      case PLAY_TIME -> characterDto.getPlayTime();
      case SKILL -> characterDto.getAvailableSkillPoints();
      case SUB_SKILL -> 1;
      default -> {
        log.warn("Unhandled point type: {}", pointType);
        yield 0L;
      }
    };
  }
}
