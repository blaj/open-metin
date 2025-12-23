package com.blaj.openmetin.game.domain.enums;

import com.blaj.openmetin.contracts.enums.ByteEnum;
import java.util.Arrays;

public enum PointType implements ByteEnum {
  LEVEL((byte) 1),
  EXPERIENCE((byte) 3),
  NEEDED_EXPERIENCE((byte) 4),
  HP((byte) 5),
  MAX_HP((byte) 6),
  SP((byte) 7),
  MAX_SP((byte) 8),
  GOLD((byte) 11),
  ST((byte) 12),
  HT((byte) 13),
  DX((byte) 14),
  IQ((byte) 15),
  DEFENCE_GRADE((byte) 16),
  ATTACK_SPEED((byte) 17),
  ATTACK_GRADE((byte) 18),
  MOVE_SPEED((byte) 19),
  DEFENCE((byte) 20),
  MAGIC_ATTACK_GRADE((byte) 22),
  MAGIC_DEFENCE_GRADE((byte) 23),
  STATUS_POINTS((byte) 26),
  SUB_SKILL((byte) 27),
  SKILL((byte) 28),
  MIN_ATTACK_DAMAGE((byte) 29),
  MAX_ATTACK_DAMAGE((byte) 30),
  PLAY_TIME((byte) 31),
  CRITICAL_PERCENTAGE((byte) 40),
  PENETRATE_PERCENTAGE((byte) 41),
  ATTACK_BONUS_HUMAN((byte) 43),
  ATTACK_BONUS_ANIMAL((byte) 44),
  ATTACK_BONUS_ORC((byte) 45),
  ATTACK_BONUS_ESOTERICS((byte) 46),
  ATTACK_BONUS_UNDEAD((byte) 47),
  ATTACK_BONUS_DEVIL((byte) 48),
  ATTACK_BONUS_INSECT((byte) 49),
  ATTACK_BONUS_FIRE((byte) 50),
  ATTACK_BONUS_ICE((byte) 51),
  ATTACK_BONUS_DESERT((byte) 52),
  ATTACK_BONUS_MONSTER((byte) 53),
  ATTACK_BONUS_WARRIOR((byte) 54),
  ATTACK_BONUS_ASSASSIN((byte) 55),
  ATTACK_BONUS_SURA((byte) 56),
  ATTACK_BONUS_SHAMAN((byte) 57),
  ATTACK_BONUS_TREE((byte) 58),
  RESIST_WARRIOR((byte) 59),
  RESIST_ASSASSIN((byte) 60),
  RESIST_SURA((byte) 61),
  RESIST_SHAMAN((byte) 62),
  BLOCK((byte) 67),
  DODGE((byte) 68),
  RESIST_SWORD((byte) 69),
  RESIST_TWO_HANDED((byte) 70),
  RESIST_DAGGER((byte) 71),
  RESIST_BELL((byte) 72),
  RESIST_FAN((byte) 73),
  RESIST_BOW((byte) 74),
  RESIST_FIRE((byte) 75),
  RESIST_ELECTRIC((byte) 76),
  RESIST_MAGIC((byte) 77),
  RESIST_WIND((byte) 78),
  ITEM_DROP_BONUS((byte) 85),
  ATTACK_BONUS((byte) 93),
  DEFENCE_BONUS((byte) 94),
  HORSE_SKILL((byte) 113),
  MALL_ATT_BONUS((byte) 114),
  MALL_DEF_BONUS((byte) 115),
  MALL_EXP_BONUS((byte) 116),
  MALL_ITEM_BONUS((byte) 117),
  MALL_GOLD_BONUS((byte) 118),
  SKILL_DAMAGE_BONUS((byte) 121),
  NORMAL_HIT_DAMAGE_BONUS((byte) 122),
  SKILL_DEFEND_BONUS((byte) 123),
  NORMAL_HIT_DEFEND_BONUS((byte) 124),
  MAGIC_ATTACK_BONUS((byte) 132),
  RESIST_ICE((byte) 133),
  RESIST_EARTH((byte) 134),
  RESIST_DARK((byte) 135),
  RESIST_CRITICAL((byte) 136),
  RESIST_PENETRATE((byte) 137),
  MIN_WEAPON_DAMAGE((byte) 200),
  MAX_WEAPON_DAMAGE((byte) 201);

  private final byte value;

  PointType(byte value) {
    this.value = value;
  }

  public static PointType fromValue(int value) {
    return Arrays.stream(values())
        .filter(pointType -> pointType.getValue() == value)
        .findFirst()
        .orElse(null);
  }

  @Override
  public byte getValue() {
    return value;
  }
}
